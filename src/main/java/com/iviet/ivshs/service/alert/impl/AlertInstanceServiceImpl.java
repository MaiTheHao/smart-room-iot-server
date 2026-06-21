package com.iviet.ivshs.service.alert.impl;

import com.iviet.ivshs.dao.AlertConfigDao;
import com.iviet.ivshs.dao.AlertInstanceDao;
import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dto.alert.AlertFilterDto;
import com.iviet.ivshs.dto.alert.AlertInstanceDto;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.service.alert.AlertMessageTemplateService;
import com.iviet.ivshs.service.alert.AlertInstanceService;
import com.iviet.ivshs.shared.enumeration.*;
import com.iviet.ivshs.shared.exception.ForbiddenException;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.shared.util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertInstanceServiceImpl implements AlertInstanceService {

    private final AlertInstanceDao alertInstanceDao;
    private final ClientDao clientDao;
    private final AlertConfigDao alertConfigDao;
    private final AlertMessageTemplateService alertMessageTemplateService;

    @Override
    @Transactional
    public AlertInstance createActiveAlert(AlertConfig config, Map<String, Object> messageTemplateData) {
        List<SysGroup> recipientGroups = alertConfigDao.findGroupsByConfigId(config.getId());
        String message = alertMessageTemplateService.buildMessage(config.getMessageTemplate(), messageTemplateData);

        AlertInstance alert = AlertInstance.builder().alertConfig(config).title(config.getAlertName()).body(message)
                .severity(config.getSeverity()).status(AlertStatus.ACTIVE).triggeredAt(Instant.now()).triggerCount(1)
                .build();
        alertInstanceDao.save(alert);
        alertInstanceDao.flush();

        for (SysGroup group : recipientGroups) {
            alertInstanceDao.saveRecipientGroupAssociation(alert, group);
        }
        return alert;
    }

    @Override
    @Transactional
    public AlertInstance incrementTriggerCount(Long alertId) {
        AlertInstance existing = alertInstanceDao.findById(alertId).orElseThrow();
        existing.setTriggerCount(existing.getTriggerCount() + 1);
        existing.setTriggeredAt(Instant.now()); // Reset lại thời gian để tính mốc cooldown mới
        alertInstanceDao.update(existing);
        return existing;
    }

    @Override
    @Transactional
    public AlertInstance updateStatus(Long alertId, AlertStatus status, AlertActorType actorType, String actorId) {
        AlertInstance alert = alertInstanceDao.findById(alertId)
                .orElseThrow(() -> new NotFoundException("Cảnh báo không tồn tại: " + alertId));

        Client currentClient = null;
        if (actorType == AlertActorType.USER) {
            Long parsedClientId = null;
            if (actorId != null && !actorId.isBlank()) {
                try {
                    parsedClientId = Long.valueOf(actorId);
                } catch (NumberFormatException e) {
                    log.warn("Không thể chuyển đổi actorId sang kiểu Long: '{}'", actorId);
                }
            }
            if (parsedClientId != null) {
                final Long currentClientId = parsedClientId;
                checkHandleAccess(alert, currentClientId);
                currentClient = clientDao.findById(currentClientId)
                        .orElseThrow(() -> new NotFoundException("Client không tồn tại: " + currentClientId));
            }
        }

        alert.setStatus(status);
        if (status == AlertStatus.ACKNOWLEDGED) {
            alert.setAcknowledgedAt(Instant.now());
            alert.setAcknowledgedBy(currentClient);
        } else if (status == AlertStatus.RESOLVED) {
            alert.setResolvedAt(Instant.now());
            alert.setResolvedBy(currentClient);
        }
        alertInstanceDao.update(alert);
        return alert;
    }

    @Override
    public PaginatedResponse<AlertInstanceDto> getAlerts(AlertFilterDto filter) {
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        List<AlertInstance> alerts = List.of();
        long total = 0;

        if (SecurityContextUtil.hasPermission(SysFunctionEnum.F_ACCESS_ALERT.getCode())) {
            alerts = alertInstanceDao.findAllByClientGroups(currentClientId, filter.status(), filter.severity(),
                    filter.page(), filter.size());
            total = alertInstanceDao.countByClientGroups(currentClientId, filter.status(), filter.severity());
        }

        List<AlertInstanceDto> dtos = alerts.stream().map(AlertInstanceDto::from).toList();
        return new PaginatedResponse<>(dtos, filter.page(), filter.size(), total);
    }

    @Override
    public PaginatedResponse<AlertInstanceDto> getAlertsBySource(AlertNamespace namespace, String sourceId,
            AlertFilterDto filter) {
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        List<AlertInstance> alerts = List.of();
        long total = 0;

        if (SecurityContextUtil.hasPermission(SysFunctionEnum.F_ACCESS_ALERT.getCode())) {
            alerts = alertInstanceDao.findAllBySourceAndClientGroups(currentClientId, namespace, sourceId,
                    filter.status(), filter.severity(), filter.page(), filter.size());
            total = alertInstanceDao.countBySourceAndClientGroups(currentClientId, namespace, sourceId, filter.status(),
                    filter.severity());
        }

        List<AlertInstanceDto> dtos = alerts.stream().map(AlertInstanceDto::from).toList();
        return new PaginatedResponse<>(dtos, filter.page(), filter.size(), total);
    }

    @Override
    public AlertInstanceDto getAlertById(Long alertId) {
        AlertInstance alert = alertInstanceDao.findById(alertId)
                .orElseThrow(() -> new NotFoundException("Cảnh báo không tồn tại: " + alertId));
        checkReadAccess(alert, SecurityContextUtil.getCurrentClientId());
        return AlertInstanceDto.from(alert);
    }

    @Override
    public PaginatedResponse<AlertInstanceDto> getAlertsByConfig(Long alertConfigId, AlertFilterDto filter) {
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        List<AlertInstance> alerts = List.of();
        long total = 0;

        if (SecurityContextUtil.hasPermission(SysFunctionEnum.F_ACCESS_ALERT.getCode())) {
            alerts = alertInstanceDao.findAllByConfigAndClientGroups(currentClientId, alertConfigId, filter.status(),
                    filter.severity(), filter.page(), filter.size());
            total = alertInstanceDao.countByConfigAndClientGroups(currentClientId, alertConfigId, filter.status(),
                    filter.severity());
        }

        List<AlertInstanceDto> dtos = alerts.stream().map(AlertInstanceDto::from).toList();
        return new PaginatedResponse<>(dtos, filter.page(), filter.size(), total);
    }

    private void checkReadAccess(AlertInstance alert, Long currentClientId) {
        SecurityContextUtil.requirePermission(SysFunctionEnum.F_ACCESS_ALERT.getCode(),
                "Bạn không có quyền truy cập cảnh báo.");
        boolean inGroup = alertInstanceDao.isClientInAlertGroups(alert.getId(), currentClientId);
        if (!inGroup) throw new ForbiddenException("Bạn không có quyền xem cảnh báo này: " + alert.getId());
    }

    private void checkHandleAccess(AlertInstance alert, Long currentClientId) {
        SecurityContextUtil.requirePermission(SysFunctionEnum.F_HANDLE_ALERT.getCode(),
                "Bạn không có quyền xử lý cảnh báo.");
        boolean inGroup = alertInstanceDao.isClientInAlertGroups(alert.getId(), currentClientId);
        if (!inGroup) throw new ForbiddenException("Bạn không có quyền xử lý cảnh báo này: " + alert.getId());
    }
}
