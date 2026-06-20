package com.iviet.ivshs.service.alert.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dao.AlertInstanceDao;
import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dao.RuleActionAlertDao;
import com.iviet.ivshs.dao.RuleDao;
import com.iviet.ivshs.dao.SysGroupDao;
import com.iviet.ivshs.dto.alert.AlertFilterDto;
import com.iviet.ivshs.dto.alert.AlertResponseDto;
import com.iviet.ivshs.dto.alert.RuleActionAlertDto;
import com.iviet.ivshs.dto.alert.SaveRuleActionAlertDto;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.entities.AlertInstance;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.Rule;
import com.iviet.ivshs.entities.RuleActionAlert;
import com.iviet.ivshs.service.alert.AlertService;
import com.iviet.ivshs.service.notification.NotificationService;
import com.iviet.ivshs.service.notification.channel.NotificationChannel;
import com.iviet.ivshs.service.notification.request.NotificationRequest;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.SysGroupEnum;
import com.iviet.ivshs.shared.exception.ForbiddenException;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.shared.util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private final AlertInstanceDao    alertInstanceDao;
    private final RuleActionAlertDao  ruleActionAlertDao;
    private final ClientDao           clientDao;
    private final SysGroupDao         sysGroupDao;
    private final NotificationService notificationService;
    private final RuleDao             ruleDao;
    private final ObjectMapper        objectMapper;

    // =====================================================================
    // Alert Configuration Methods
    // =====================================================================

    @Override
    public List<RuleActionAlertDto> getAlertConfigsByRuleId(Long ruleId) {
        return ruleActionAlertDao.findAllByRuleId(ruleId).stream()
                .map(RuleActionAlertDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<RuleActionAlertDto> saveAlertConfigs(Long ruleId, List<SaveRuleActionAlertDto> dtos) {
        Rule rule = ruleDao.findById(ruleId)
                .orElseThrow(() -> new NotFoundException("Rule not found with ID: " + ruleId));

        // Collect IDs from incoming DTO list to delete orphans
        Set<Long> incomingIds = dtos.stream()
                .map(SaveRuleActionAlertDto::id)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        // Remove configs from rule.getAlerts() whose IDs are not in the new incoming list
        List<RuleActionAlert> toRemove = rule.getAlerts().stream()
                .filter(config -> config.getId() != null && !incomingIds.contains(config.getId()))
                .collect(Collectors.toList());
        for (RuleActionAlert config : toRemove) {
            rule.removeAlertConfig(config);
            ruleActionAlertDao.delete(config);
        }

        // Process incoming DTOs
        for (SaveRuleActionAlertDto dto : dtos) {
            if (dto.id() != null) {
                // Update existing
                RuleActionAlert config = rule.getAlerts().stream()
                        .filter(c -> dto.id().equals(c.getId()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("Alert config not found with ID: " + dto.id()));
                config.setAlertName(dto.alertName());
                config.setSeverity(dto.severity());
                config.setRecipientGroups(objectMapper.valueToTree(dto.recipientGroups()));
                config.setChannels(objectMapper.valueToTree(dto.channels()));
                config.setMessageTemplate(dto.messageTemplate());
                config.setCooldownMinutes(dto.cooldownMinutes());
                config.setAutoResolve(dto.autoResolve());
                ruleActionAlertDao.update(config);
            } else {
                // Create new
                RuleActionAlert config = new RuleActionAlert();
                config.setAlertName(dto.alertName());
                config.setSeverity(dto.severity());
                config.setRecipientGroups(objectMapper.valueToTree(dto.recipientGroups()));
                config.setChannels(objectMapper.valueToTree(dto.channels()));
                config.setMessageTemplate(dto.messageTemplate());
                config.setCooldownMinutes(dto.cooldownMinutes());
                config.setAutoResolve(dto.autoResolve());
                rule.addAlertConfig(config);
                ruleActionAlertDao.save(config);
            }
        }

        ruleDao.update(rule);

        return rule.getAlerts().stream()
                .map(RuleActionAlertDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAlertsByRuleId(Long ruleId) {
        List<RuleActionAlert> configs = ruleActionAlertDao.findAllByRuleId(ruleId);
        for (RuleActionAlert config : configs) {
            ruleActionAlertDao.delete(config);
        }
    }

    // =====================================================================
    // Rule Engine Integration
    // =====================================================================

    @Override
    @Transactional
    public void triggerAlert(Long alertConfigId) {
        // 1. Tìm config alert.
        RuleActionAlert config = ruleActionAlertDao.findById(alertConfigId)
                .orElseThrow(() -> new NotFoundException("Alert config not found with ID: " + alertConfigId));

        // 2. Kiểm tra cooldown: nếu còn alert đang mở trong cửa sổ cooldown → bỏ qua.
        if (config.getCooldownMinutes() > 0) {
            Optional<AlertInstance> latestOpen = alertInstanceDao.findLatestOpenByConfigId(config.getId());
            if (latestOpen.isPresent()) {
                Instant cooldownEnd = latestOpen.get().getTriggeredAt()
                        .plusSeconds(config.getCooldownMinutes() * 60L);
                if (Instant.now().isBefore(cooldownEnd)) {
                    log.info("[Alert] Alert config {} is in cooldown until {} — skipping trigger", config.getId(), cooldownEnd);
                    return;
                }
            }
        }

        // 3. Resolve danh sách recipients từ group codes trong config.
        Set<Client> recipients = resolveRecipients(config.getRecipientGroups());

        // 4. Tạo và lưu AlertInstance (status = ACTIVE).
        AlertInstance alert = AlertInstance.builder()
                .alertConfig(config)
                .title(config.getAlertName())
                .body(config.getMessageTemplate())
                .severity(config.getSeverity())
                .status(AlertStatus.ACTIVE)
                .triggeredAt(Instant.now())
                .recipients(new HashSet<>(recipients))
                .build();
        alertInstanceDao.save(alert);
        alertInstanceDao.flush(); // Flush để có ID trước khi dùng trong FCM data

        log.info("[Alert] AlertInstance created: id={}, configId={}, severity={}, recipients={}",
                alert.getId(), config.getId(), config.getSeverity(), recipients.size());

        // 5. Build FCM data payload (tất cả values phải là String theo FCM v1 spec).
        Map<String, String> data = buildFcmData("ALERT_TRIGGERED", alert, "ACTIVE");

        // 6. Parse danh sách channels từ JSON config.
        List<NotificationChannel> channels = parseJsonStringArray(config.getChannels()).stream()
                .map(NotificationChannel::fromValue)
                .collect(Collectors.toList());

        // 7. Load lại recipients với clientDevices (LAZY) trong transaction hiện tại.
        Set<Client> recipientsWithDevices = loadRecipientsWithDevices(recipients);

        // 8. Build request
        NotificationRequest request = NotificationRequest.builder()
                .recipients(recipientsWithDevices)
                .channels(channels)
                .title(config.getAlertName())
                .body(config.getMessageTemplate())
                .data(data)
                .build();

        // 9. Dispatch qua NotificationService (Strategy Pattern: FCM/Email/SMS).
        notificationService.sendNotification(request);
    }

    @Override
    @Transactional
    public void resolveAlertIfNeeded(Long alertConfigId) {
        // 1. Tìm config alert.
        RuleActionAlert config = ruleActionAlertDao.findById(alertConfigId)
                .orElseThrow(() -> new NotFoundException("Alert config not found with ID: " + alertConfigId));

        if (Boolean.FALSE.equals(config.getAutoResolve())) {
            return;
        }

        // 2. Tìm tất cả alert đang mở (đã FETCH recipients).
        List<AlertInstance> openAlerts = alertInstanceDao.findAllOpenByConfigId(alertConfigId);
        if (openAlerts.isEmpty()) return;

        List<NotificationChannel> channels = parseJsonStringArray(config.getChannels()).stream()
                .map(NotificationChannel::fromValue)
                .collect(Collectors.toList());
        Instant now = Instant.now();

        for (AlertInstance alert : openAlerts) {
            // 3. Cập nhật trạng thái RESOLVED (resolved_by = null = auto-resolved bởi hệ thống).
            alert.setStatus(AlertStatus.RESOLVED);
            alert.setResolvedAt(now);
            alert.setResolvedBy(null);
            alertInstanceDao.update(alert);

            log.info("[Alert] Auto-resolved AlertInstance id={} for Alert Config {}", alert.getId(), alertConfigId);

            // 4. Gửi recovery notification.
            Map<String, String> data = buildFcmData("ALERT_RESOLVED", alert, "RESOLVED");
            String resolvedTitle = "Cảnh báo đã phục hồi: " + config.getAlertName();
            String resolvedBody  = "Tình trạng đã trở lại bình thường.";

            Set<Client> recipientsWithDevices = loadRecipientsWithDevices(alert.getRecipients());
            
            NotificationRequest request = NotificationRequest.builder()
                    .recipients(recipientsWithDevices)
                    .channels(channels)
                    .title(resolvedTitle)
                    .body(resolvedBody)
                    .data(data)
                    .build();

            notificationService.sendNotification(request);
        }
    }

    // =====================================================================
    // REST API Methods
    // =====================================================================

    @Override
    public PaginatedResponse<AlertResponseDto> getAlerts(AlertFilterDto filter) {
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        List<String> userGroups = SecurityContextUtil.getCurrentUsername() != null ?
                SecurityContextUtil.getCurrentFunctions() != null ? 
                new ArrayList<>(clientDao.findById(currentClientId)
                        .orElseThrow(() -> new NotFoundException("Client not found"))
                        .getGroups().stream().map(g -> g.getGroupCode()).collect(Collectors.toList())) : List.of() : List.of();

        List<AlertInstance> alerts;
        long total;

        if (userGroups.contains(SysGroupEnum.G_ADMIN.getCode())) {
            // Admin: xem toàn bộ alerts trong hệ thống
            alerts = alertInstanceDao.findAll(filter.status(), filter.severity(), filter.page(), filter.size());
            total  = alertInstanceDao.countAll(filter.status(), filter.severity());

        } else if (userGroups.contains(SysGroupEnum.G_MAINTENANCE.getCode())) {
            // Maintenance: xem alerts được gửi tới group G_MAINTENANCE
            String groupCode = SysGroupEnum.G_MAINTENANCE.getCode();
            alerts = alertInstanceDao.findAllByGroupCode(groupCode, filter.status(), filter.severity(), filter.page(), filter.size());
            total  = alertInstanceDao.countByGroupCode(groupCode, filter.status(), filter.severity());

        } else {
            // End user: chỉ xem "My Alerts" — những alert mà mình là recipient
            alerts = alertInstanceDao.findAllByRecipientClientId(currentClientId, filter.status(), filter.severity(), filter.page(), filter.size());
            total  = alertInstanceDao.countByRecipientClientId(currentClientId, filter.status(), filter.severity());
        }

        List<AlertResponseDto> dtos = alerts.stream()
                .map(AlertResponseDto::from)
                .collect(Collectors.toList());
        return new PaginatedResponse<>(dtos, filter.page(), filter.size(), total);
    }

    @Override
    public AlertResponseDto getAlertById(Long alertId) {
        AlertInstance alert = alertInstanceDao.findByIdWithRecipients(alertId)
                .orElseThrow(() -> new NotFoundException("Alert not found: " + alertId));
        checkReadAccess(alert);
        return AlertResponseDto.from(alert);
    }

    @Override
    @Transactional
    public AlertResponseDto acknowledge(Long alertId) {
        AlertInstance alert = alertInstanceDao.findByIdWithRecipients(alertId)
                .orElseThrow(() -> new NotFoundException("Alert not found: " + alertId));
        checkReadAccess(alert);

        if (alert.getStatus() == AlertStatus.ACTIVE) {
            Long currentClientId = SecurityContextUtil.getCurrentClientId();
            Client currentClient = clientDao.findById(currentClientId)
                    .orElseThrow(() -> new NotFoundException("Client not found: " + currentClientId));
            alert.setStatus(AlertStatus.ACKNOWLEDGED);
            alert.setAcknowledgedAt(Instant.now());
            alert.setAcknowledgedBy(currentClient);
            alertInstanceDao.update(alert);
            log.info("[Alert] Alert {} acknowledged by client {}", alertId, currentClientId);
        }
        return AlertResponseDto.from(alert);
    }

    @Override
    @Transactional
    public AlertResponseDto resolve(Long alertId) {
        AlertInstance alert = alertInstanceDao.findByIdWithRecipients(alertId)
                .orElseThrow(() -> new NotFoundException("Alert not found: " + alertId));
        checkReadAccess(alert);

        if (alert.getStatus() != AlertStatus.RESOLVED) {
            Long currentClientId = SecurityContextUtil.getCurrentClientId();
            Client currentClient = clientDao.findById(currentClientId)
                    .orElseThrow(() -> new NotFoundException("Client not found: " + currentClientId));
            alert.setStatus(AlertStatus.RESOLVED);
            alert.setResolvedAt(Instant.now());
            alert.setResolvedBy(currentClient);
            alertInstanceDao.update(alert);
            log.info("[Alert] Alert {} manually resolved by client {}", alertId, currentClientId);

            // Dispatch ALERT_RESOLVED push notification
            List<RuleActionAlert> configs = ruleActionAlertDao.findAllByRuleId(alert.getAlertConfig().getRule().getId());
            for (RuleActionAlert config : configs) {
                List<NotificationChannel> channels = parseJsonStringArray(config.getChannels()).stream()
                        .map(NotificationChannel::fromValue)
                        .collect(Collectors.toList());
                Map<String, String> data = buildFcmData("ALERT_RESOLVED", alert, "RESOLVED");
                String resolvedTitle = "Cảnh báo đã giải quyết: " + config.getAlertName();
                String resolvedBody  = "Sự cố đã được xử lý bởi " + currentClient.getUsername() + ".";
                Set<Client> recipientsWithDevices = loadRecipientsWithDevices(alert.getRecipients());
                
                NotificationRequest request = NotificationRequest.builder()
                        .recipients(recipientsWithDevices)
                        .channels(channels)
                        .title(resolvedTitle)
                        .body(resolvedBody)
                        .data(data)
                        .build();

                notificationService.sendNotification(request);
            }
        }
        return AlertResponseDto.from(alert);
    }

    // =====================================================================
    // Private Helpers
    // =====================================================================

    /**
     * Đọc mảng JSON group codes và trả về tất cả Client thuộc các groups đó.
     * Input: JsonNode đại diện cho ["G_ADMIN", "G_MAINTENANCE"]
     */
    private Set<Client> resolveRecipients(JsonNode recipientGroups) {
        Set<Client> recipients = new HashSet<>();
        if (recipientGroups == null || !recipientGroups.isArray()) {
            return recipients;
        }
        StreamSupport.stream(recipientGroups.spliterator(), false)
                .map(JsonNode::asText)
                .filter(code -> !code.isBlank())
                .forEach(groupCode ->
                    sysGroupDao.findEntityByCode(groupCode).ifPresent(group -> {
                        List<Client> groupClients = sysGroupDao.findClientEntitiesByGroupId(group.getId());
                        recipients.addAll(groupClients);
                    })
                );
        log.debug("[Alert] Resolved {} unique recipients from groups {}", recipients.size(), recipientGroups);
        return recipients;
    }

    /**
     * Load lại tập Client với clientDevices được FETCH trong transaction hiện tại.
     * Cần thiết vì Client.clientDevices = FetchType.LAZY —
     * nếu không reload, FcmNotificationStrategy sẽ gặp LazyInitializationException.
     */
    private Set<Client> loadRecipientsWithDevices(Set<Client> recipients) {
        if (recipients == null || recipients.isEmpty()) return Set.of();
        return recipients.stream()
                .map(c -> clientDao.findById(c.getId()).orElse(null))
                .filter(c -> c != null)
                .collect(Collectors.toSet());
    }

    /**
     * Build FCM v1 data map. TẤT CẢ values phải là String theo FCM v1 API spec.
     * Deep link format: smartroom://alert/{id}
     */
    private Map<String, String> buildFcmData(String type, AlertInstance alert, String statusStr) {
        Map<String, String> data = new HashMap<>();
        data.put("type",      type);
        data.put("entityId",  String.valueOf(alert.getId()));
        data.put("severity",  alert.getSeverity().name());
        data.put("status",    statusStr);
        data.put("deepLink",  "smartroom://alert/" + alert.getId());
        data.put("timestamp", String.valueOf(Instant.now().toEpochMilli()));
        return data;
    }

    /**
     * Parse JsonNode mảng strings thành List<String>.
     * Trả về List.of() nếu node là null hoặc không phải array.
     */
    private List<String> parseJsonStringArray(JsonNode jsonNode) {
        if (jsonNode == null || !jsonNode.isArray()) return List.of();
        List<String> result = new ArrayList<>();
        jsonNode.forEach(node -> {
            String text = node.asText();
            if (!text.isBlank()) result.add(text);
        });
        return result;
    }

    /**
     * Kiểm tra quyền đọc alert của user hiện tại:
     * G_ADMIN    → luôn pass
     * G_MAINTENANCE → pass nếu G_MAINTENANCE trong recipient_groups config của Rule
     * G_USER     → pass chỉ khi clientId có trong alert.recipients (alert_recipient table)
     */
    private void checkReadAccess(AlertInstance alert) {
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        List<String> userGroups = clientDao.findById(currentClientId)
                .orElseThrow(() -> new NotFoundException("Client not found"))
                .getGroups().stream().map(g -> g.getGroupCode()).collect(Collectors.toList());

        if (userGroups.contains(SysGroupEnum.G_ADMIN.getCode())) return;

        if (userGroups.contains(SysGroupEnum.G_MAINTENANCE.getCode())) {
            List<RuleActionAlert> configs = ruleActionAlertDao.findAllByRuleId(alert.getAlertConfig().getRule().getId());
            for (RuleActionAlert config : configs) {
                JsonNode groups = config.getRecipientGroups();
                if (groups != null && groups.isArray()) {
                    boolean hasAccess = StreamSupport.stream(groups.spliterator(), false)
                            .anyMatch(n -> SysGroupEnum.G_MAINTENANCE.getCode().equals(n.asText()));
                    if (hasAccess) return;
                }
            }
        }

        // G_USER: kiểm tra danh sách recipient
        boolean isRecipient = alert.getRecipients().stream()
                .anyMatch(c -> currentClientId.equals(c.getId()));
        if (isRecipient) return;

        throw new ForbiddenException("You do not have access to alert " + alert.getId());
    }
}
