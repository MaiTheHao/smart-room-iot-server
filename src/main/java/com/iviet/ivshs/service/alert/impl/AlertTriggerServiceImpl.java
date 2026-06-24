package com.iviet.ivshs.service.alert.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dao.AlertConfigDao;
import com.iviet.ivshs.dao.AlertInstanceDao;
import com.iviet.ivshs.dto.alert.AlertTriggerRequestDto;
import com.iviet.ivshs.dto.alert.CreateAlertInstanceLogDto;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.service.alert.AlertInstanceLogService;
import com.iviet.ivshs.service.alert.AlertInstanceService;
import com.iviet.ivshs.service.alert.AlertMessageTemplateService;
import com.iviet.ivshs.service.alert.AlertTriggerService;
import com.iviet.ivshs.service.alert.event.AlertNotificationEvent;
import com.iviet.ivshs.shared.enumeration.*;
import com.iviet.ivshs.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertTriggerServiceImpl implements AlertTriggerService {

    private final AlertInstanceService alertInstanceService;
    private final AlertInstanceLogService alertInstanceLogService;
    private final AlertConfigDao alertConfigDao;
    private final AlertInstanceDao alertInstanceDao;
    private final ApplicationEventPublisher eventPublisher;
    private final AlertMessageTemplateService alertMessageTemplateService;

    @Override
    @Transactional
    public void trigger(AlertTriggerRequestDto request) {
        AlertActionType actionType = request.getActionType();
        if (actionType != AlertActionType.TRIGGERED) {
            log.warn("Invalid action type {} for trigger method.", actionType);
            return;
        }

        AlertInstance alert = null;
        String logMessage = request.getLogMessage();
        AlertConfig config = request.getAlertConfig();

        if (config == null && request.getAlertConfigId() != null) {
            config = alertConfigDao.findById(request.getAlertConfigId())
                    .orElseThrow(() -> new NotFoundException("Alert configuration not found"));
        }

        if (config == null) {
            log.warn("Failed to trigger alert: Missing alert configuration.");
            return;
        }

        Optional<AlertInstance> latestOpen = alertInstanceDao.findLatestOpenByConfigId(config.getId());
        if (latestOpen.isPresent()) {
            AlertInstance existingAlert = latestOpen.get();
            if (config.getCooldownMinutes() > 0) {
                Instant cooldownEnd = existingAlert.getTriggeredAt().plusSeconds(config.getCooldownMinutes() * 60L);
                if (Instant.now().isBefore(cooldownEnd)) {
                    log.debug("Alert config ID {} is in cooldown. Skipped.", config.getId());
                    return;
                }
            }
            alert = alertInstanceService.incrementTriggerCount(existingAlert.getId());
            actionType = AlertActionType.RE_TRIGGERED;
            if (logMessage == null) {
                logMessage = alertMessageTemplateService.buildMessage(config.getMessageTemplate(), request.getTemplateData());
            }
        } else {
            alert = alertInstanceService.createActiveAlert(config, request.getTemplateData());
            logMessage = logMessage != null ? logMessage : "Alert started";
        }

        CreateAlertInstanceLogDto logDto = CreateAlertInstanceLogDto.builder().alertId(alert.getId())
                .actionType(actionType).actorType(request.getActorType()).actorId(request.getActorId())
                .message(logMessage).payload(request.getPayload()).build();
        alertInstanceLogService.createLog(logDto);
        eventPublisher.publishEvent(new AlertNotificationEvent(this, config, alert, actionType, request.getActorType(),
                request.getActorId(), logMessage));
    }

    @Override
    @Transactional
    public void handleAction(Long alertInstanceId, AlertActionType actionType, AlertActorType actorType, String actorId,
            String logMessage, JsonNode payload) {

        AlertStatus status = switch (actionType) {
            case ACKNOWLEDGED -> AlertStatus.ACKNOWLEDGED;
            case RESOLVED, AUTO_RESOLVED -> AlertStatus.RESOLVED;
            default -> throw new IllegalArgumentException("Unsupported action type for handleAction: " + actionType);
        };

        AlertInstance alert = alertInstanceService.updateStatus(alertInstanceId, status, actorType, actorId);
        AlertConfig config = alert.getAlertConfig();

        if (logMessage == null) {
            logMessage = (actionType == AlertActionType.ACKNOWLEDGED) ? "Alert acknowledged" : "Alert resolved";
        }

        CreateAlertInstanceLogDto logDto = CreateAlertInstanceLogDto.builder().alertId(alert.getId())
                .actionType(actionType).actorType(actorType).actorId(actorId).message(logMessage).payload(payload)
                .build();
        alertInstanceLogService.createLog(logDto);
        eventPublisher.publishEvent(new AlertNotificationEvent(this, config, alert, actionType, actorType, actorId, logMessage));
    }
}
