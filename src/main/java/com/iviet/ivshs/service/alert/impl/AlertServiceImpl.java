package com.iviet.ivshs.service.alert.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dao.AlertConfigDao;
import com.iviet.ivshs.dao.AlertIncidentLogDao;
import com.iviet.ivshs.dao.AlertRecipientDao;
import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dao.SysGroupDao;
import com.iviet.ivshs.dto.alert.AlertFilterDto;
import com.iviet.ivshs.dto.alert.AlertResponseDto;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.dto.notification.NotificationRequest;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.service.alert.AlertService;
import com.iviet.ivshs.service.notification.NotificationService;
import com.iviet.ivshs.shared.enumeration.*;
import com.iviet.ivshs.shared.exception.ForbiddenException;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.shared.util.SecurityContextUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private final AlertConfigDao alertConfigDao;
    private final AlertRecipientDao alertRecipientDao;
    private final AlertIncidentLogDao alertIncidentLogDao;
    private final ClientDao clientDao;
    private final SysGroupDao sysGroupDao;
    private final NotificationService notificationService;

    @PersistenceContext
    private EntityManager entityManager;

    // =====================================================================
    // Rule Engine Integration
    // =====================================================================

    @Override
    @Transactional
    public void triggerAlert(Long alertConfigId) {
        AlertConfig config = alertConfigDao.findById(alertConfigId)
                .orElseThrow(() -> new NotFoundException("Alert config not found: " + alertConfigId));

        // 1. Kiểm tra cooldown
        if (config.getCooldownMinutes() > 0) {
            Optional<AlertRecipient> latestOpen = alertRecipientDao.findLatestOpenByConfigId(config.getId());
            if (latestOpen.isPresent()) {
                Instant cooldownEnd = latestOpen.get().getTriggeredAt()
                        .plusSeconds(config.getCooldownMinutes() * 60L);
                if (Instant.now().isBefore(cooldownEnd)) {
                    // Trong cooldown: tăng trigger_count + ghi log RE_TRIGGERED (không gửi FCM)
                    AlertRecipient existing = latestOpen.get();
                    existing.setTriggerCount(existing.getTriggerCount() + 1);
                    alertRecipientDao.update(existing);

                    alertIncidentLogDao.save(AlertIncidentLog.builder()
                            .alert(existing)
                            .actionType(AlertActionType.RE_TRIGGERED)
                            .actorType(AlertActorType.SYSTEM)
                            .actorId("RULE_ENGINE")
                            .message("Sự kiện tiếp diễn trong thời gian cooldown")
                            .createdAt(Instant.now())
                            .build());

                    log.info("[Alert] Config {} in cooldown until {} — incremented trigger_count to {}",
                            config.getId(), cooldownEnd, existing.getTriggerCount());
                    return;
                }
            }
        }

        // 2. Lấy danh sách group nhận tin từ alert_config_group
        List<SysGroup> recipientGroups = getConfigGroups(alertConfigId);

        // 3. Tạo AlertRecipient mới (status = ACTIVE)
        AlertRecipient alert = AlertRecipient.builder()
                .alertConfig(config)
                .title(config.getAlertName())
                .body(config.getMessageTemplate())
                .severity(config.getSeverity())
                .status(AlertStatus.ACTIVE)
                .triggeredAt(Instant.now())
                .triggerCount(1)
                .build();
        alertRecipientDao.save(alert);
        alertRecipientDao.flush();

        // 4. Lưu alert_recipient_group
        for (SysGroup group : recipientGroups) {
            AlertRecipientGroup arg = AlertRecipientGroup.builder()
                    .alert(alert)
                    .group(group)
                    .build();
            entityManager.persist(arg);
        }

        // 5. Ghi log TRIGGERED
        alertIncidentLogDao.save(AlertIncidentLog.builder()
                .alert(alert)
                .actionType(AlertActionType.TRIGGERED)
                .actorType(AlertActorType.SYSTEM)
                .actorId("RULE_ENGINE")
                .message("Sự cố bắt đầu — cấu hình [" + config.getAlertName() + "] kích hoạt")
                .createdAt(Instant.now())
                .build());

        log.info("[Alert] AlertRecipient created: id={}, configId={}, severity={}, groups={}",
                alert.getId(), config.getId(), config.getSeverity(), recipientGroups.size());

        // 6. Dispatch FCM SAU KHI COMMIT (fix Dual Write Problem)
        List<NotificationChannel> channels = parseChannels(config.getChannels());
        Set<Client> recipientsWithDevices = loadDevicesForGroups(recipientGroups);
        Map<String, String> data = buildFcmData("ALERT_TRIGGERED", alert, "ACTIVE");
        NotificationRequest request = NotificationRequest.builder()
                .recipients(recipientsWithDevices)
                .channels(channels)
                .title(config.getAlertName())
                .body(config.getMessageTemplate())
                .data(data)
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificationService.sendNotification(request);
            }
        });
    }

    @Override
    @Transactional
    public void resolveAlertIfNeeded(Long alertConfigId) {
        AlertConfig config = alertConfigDao.findById(alertConfigId)
                .orElseThrow(() -> new NotFoundException("Alert config not found: " + alertConfigId));

        if (Boolean.FALSE.equals(config.getAutoResolve())) return;

        List<AlertRecipient> openAlerts = alertRecipientDao.findAllOpenByConfigId(alertConfigId);
        if (openAlerts.isEmpty()) return;

        List<NotificationChannel> channels = parseChannels(config.getChannels());
        List<SysGroup> recipientGroups = getConfigGroups(alertConfigId);
        Instant now = Instant.now();

        for (AlertRecipient alert : openAlerts) {
            alert.setStatus(AlertStatus.RESOLVED);
            alert.setResolvedAt(now);
            alert.setResolvedBy(null);
            alertRecipientDao.update(alert);

            alertIncidentLogDao.save(AlertIncidentLog.builder()
                    .alert(alert)
                    .actionType(AlertActionType.AUTO_RESOLVED)
                    .actorType(AlertActorType.SYSTEM)
                    .actorId("RULE_ENGINE")
                    .message("Tình trạng đã trở lại bình thường. Hệ thống tự động giải quyết.")
                    .createdAt(now)
                    .build());

            log.info("[Alert] Auto-resolved AlertRecipient id={} for config {}", alert.getId(), alertConfigId);

            Set<Client> recipientsWithDevices = loadDevicesForGroups(recipientGroups);
            Map<String, String> data = buildFcmData("ALERT_RESOLVED", alert, "RESOLVED");
            String resolvedTitle = "Cảnh báo đã phục hồi: " + config.getAlertName();
            String resolvedBody = "Tình trạng đã trở lại bình thường.";
            NotificationRequest request = NotificationRequest.builder()
                    .recipients(recipientsWithDevices)
                    .channels(channels)
                    .title(resolvedTitle)
                    .body(resolvedBody)
                    .data(data)
                    .build();

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notificationService.sendNotification(request);
                }
            });
        }
    }

    // =====================================================================
    // REST API Methods
    // =====================================================================

    @Override
    public PaginatedResponse<AlertResponseDto> getAlerts(AlertFilterDto filter) {
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        Set<String> userFunctions = getCurrentUserFunctionCodes();

        List<AlertRecipient> alerts;
        long total;

        if (userFunctions.contains(SysFunctionEnum.F_ACCESS_ALERT.getCode())) {
            // Lọc dynamic theo group của user (không hardcode group name)
            alerts = alertRecipientDao.findAllByClientGroups(
                    currentClientId, filter.status(), filter.severity(), filter.page(), filter.size());
            total = alertRecipientDao.countByClientGroups(
                    currentClientId, filter.status(), filter.severity());
        } else {
            alerts = List.of();
            total = 0;
        }

        List<AlertResponseDto> dtos = alerts.stream().map(AlertResponseDto::from).collect(Collectors.toList());
        return new PaginatedResponse<>(dtos, filter.page(), filter.size(), total);
    }

    @Override
    public PaginatedResponse<AlertResponseDto> getAlertsBySource(AlertNamespace namespace, String sourceId, AlertFilterDto filter) {
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        Set<String> userFunctions = getCurrentUserFunctionCodes();

        List<AlertRecipient> alerts;
        long total;

        if (userFunctions.contains(SysFunctionEnum.F_ACCESS_ALERT.getCode())) {
            alerts = alertRecipientDao.findAllBySourceAndClientGroups(
                    currentClientId, namespace, sourceId, filter.status(), filter.severity(), filter.page(), filter.size());
            total = alertRecipientDao.countBySourceAndClientGroups(
                    currentClientId, namespace, sourceId, filter.status(), filter.severity());
        } else {
            alerts = List.of();
            total = 0;
        }

        List<AlertResponseDto> dtos = alerts.stream().map(AlertResponseDto::from).collect(Collectors.toList());
        return new PaginatedResponse<>(dtos, filter.page(), filter.size(), total);
    }

    @Override
    public AlertResponseDto getAlertById(Long alertId) {
        AlertRecipient alert = alertRecipientDao.findById(alertId)
                .orElseThrow(() -> new NotFoundException("Alert not found: " + alertId));
        checkReadAccess(alert);
        return AlertResponseDto.from(alert);
    }

    @Override
    @Transactional
    public AlertResponseDto acknowledge(Long alertId) {
        AlertRecipient alert = alertRecipientDao.findById(alertId)
                .orElseThrow(() -> new NotFoundException("Alert not found: " + alertId));
        checkHandleAccess(alert);

        if (alert.getStatus() == AlertStatus.ACTIVE) {
            Long currentClientId = SecurityContextUtil.getCurrentClientId();
            Client currentClient = clientDao.findById(currentClientId)
                    .orElseThrow(() -> new NotFoundException("Client not found: " + currentClientId));
            alert.setStatus(AlertStatus.ACKNOWLEDGED);
            alert.setAcknowledgedAt(Instant.now());
            alert.setAcknowledgedBy(currentClient);
            alertRecipientDao.update(alert);

            alertIncidentLogDao.save(AlertIncidentLog.builder()
                    .alert(alert)
                    .actionType(AlertActionType.ACKNOWLEDGED)
                    .actorType(AlertActorType.USER)
                    .actorId(String.valueOf(currentClientId))
                    .message("Sự cố đã được xác nhận bởi " + currentClient.getUsername())
                    .createdAt(Instant.now())
                    .build());

            log.info("[Alert] Alert {} acknowledged by client {}", alertId, currentClientId);
        }
        return AlertResponseDto.from(alert);
    }

    @Override
    @Transactional
    public AlertResponseDto resolve(Long alertId) {
        AlertRecipient alert = alertRecipientDao.findById(alertId)
                .orElseThrow(() -> new NotFoundException("Alert not found: " + alertId));
        checkHandleAccess(alert);

        if (alert.getStatus() != AlertStatus.RESOLVED) {
            Long currentClientId = SecurityContextUtil.getCurrentClientId();
            Client currentClient = clientDao.findById(currentClientId)
                    .orElseThrow(() -> new NotFoundException("Client not found: " + currentClientId));
            alert.setStatus(AlertStatus.RESOLVED);
            alert.setResolvedAt(Instant.now());
            alert.setResolvedBy(currentClient);
            alertRecipientDao.update(alert);

            alertIncidentLogDao.save(AlertIncidentLog.builder()
                    .alert(alert)
                    .actionType(AlertActionType.RESOLVED)
                    .actorType(AlertActorType.USER)
                    .actorId(String.valueOf(currentClientId))
                    .message("Sự cố đã được giải quyết bởi " + currentClient.getUsername())
                    .createdAt(Instant.now())
                    .build());

            log.info("[Alert] Alert {} manually resolved by client {}", alertId, currentClientId);

            // Dispatch ALERT_RESOLVED sau commit
            AlertConfig config = alert.getAlertConfig();
            List<SysGroup> recipientGroups = getConfigGroups(config.getId());
            Set<Client> recipientsWithDevices = loadDevicesForGroups(recipientGroups);
            List<NotificationChannel> channels = parseChannels(config.getChannels());
            Map<String, String> data = buildFcmData("ALERT_RESOLVED", alert, "RESOLVED");
            String resolvedBody = "Sự cố đã được xử lý bởi " + currentClient.getUsername() + ".";
            NotificationRequest request = NotificationRequest.builder()
                    .recipients(recipientsWithDevices)
                    .channels(channels)
                    .title("Cảnh báo đã giải quyết: " + config.getAlertName())
                    .body(resolvedBody)
                    .data(data)
                    .build();

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notificationService.sendNotification(request);
                }
            });
        }
        return AlertResponseDto.from(alert);
    }

    // =====================================================================
    // Private Helpers
    // =====================================================================

    /**
     * Kiểm tra quyền XEM (READ).
     * User cần có quyền F_ACCESS_ALERT VÀ thuộc ít nhất một group của alert.
     */
    private void checkReadAccess(AlertRecipient alert) {
        Set<String> userFunctions = getCurrentUserFunctionCodes();
        if (!userFunctions.contains(SysFunctionEnum.F_ACCESS_ALERT.getCode())) {
            throw new ForbiddenException("You do not have F_ACCESS_ALERT permission.");
        }
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        boolean inGroup = alertRecipientDao.isClientInAlertGroups(alert.getId(), currentClientId);
        if (!inGroup) {
            throw new ForbiddenException("You do not have access to alert " + alert.getId());
        }
    }

    /**
     * Kiểm tra quyền HANDLE (Acknowledge/Resolve).
     * User cần có quyền F_HANDLE_ALERT VÀ thuộc ít nhất một group của alert.
     */
    private void checkHandleAccess(AlertRecipient alert) {
        Set<String> userFunctions = getCurrentUserFunctionCodes();
        if (!userFunctions.contains(SysFunctionEnum.F_HANDLE_ALERT.getCode())) {
            throw new ForbiddenException("You do not have F_HANDLE_ALERT permission.");
        }
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        boolean inGroup = alertRecipientDao.isClientInAlertGroups(alert.getId(), currentClientId);
        if (!inGroup) {
            throw new ForbiddenException("You do not have access to alert " + alert.getId());
        }
    }

    private List<SysGroup> getConfigGroups(Long alertConfigId) {
        String jpql = "SELECT acg.group FROM AlertConfigGroup acg WHERE acg.alertConfig.id = :id";
        return entityManager.createQuery(jpql, SysGroup.class)
                .setParameter("id", alertConfigId)
                .getResultList();
    }

    /**
     * Truy vấn động: lấy FCM tokens của tất cả user đang thuộc các group nhận tin.
     * Thay thế resolveRecipients cũ — không còn snapshot client_id.
     */
    private Set<Client> loadDevicesForGroups(List<SysGroup> groups) {
        if (groups == null || groups.isEmpty()) return Set.of();
        Set<Long> groupIds = groups.stream().map(SysGroup::getId).collect(Collectors.toSet());
        List<Client> clients = entityManager.createQuery(
                "SELECT DISTINCT c FROM SysGroup g JOIN g.clients c WHERE g.id IN :groupIds", Client.class)
                .setParameter("groupIds", groupIds)
                .getResultList();
        if (clients.isEmpty()) return Set.of();
        Set<Long> clientIds = clients.stream().map(Client::getId).collect(Collectors.toSet());
        return clientDao.findAllWithDevicesByIdIn(clientIds);
    }

    private Set<String> getCurrentUserFunctionCodes() {
        try {
            return SecurityContextUtil.getCurrentFunctions();
        } catch (Exception e) {
            return Set.of();
        }
    }

    private Map<String, String> buildFcmData(String type, AlertRecipient alert, String statusStr) {
        Map<String, String> data = new HashMap<>();
        data.put("type", type);
        data.put("entityId", String.valueOf(alert.getId()));
        data.put("severity", alert.getSeverity().name());
        data.put("status", statusStr);
        data.put("deepLink", "smartroom://alert/" + alert.getId());
        data.put("timestamp", String.valueOf(Instant.now().toEpochMilli()));
        return data;
    }

    private List<NotificationChannel> parseChannels(JsonNode jsonNode) {
        if (jsonNode == null || !jsonNode.isArray()) return List.of();
        List<NotificationChannel> result = new ArrayList<>();
        jsonNode.forEach(node -> {
            String text = node.asText();
            if (!text.isBlank()) result.add(NotificationChannel.fromValue(text));
        });
        return result;
    }
}
