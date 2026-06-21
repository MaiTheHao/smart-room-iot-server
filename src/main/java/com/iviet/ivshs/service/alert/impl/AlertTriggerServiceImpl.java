package com.iviet.ivshs.service.alert.impl;

import com.iviet.ivshs.dao.AlertConfigDao;
import com.iviet.ivshs.dao.AlertInstanceDao;
import com.iviet.ivshs.dto.alert.AlertTriggerRequestDto;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.service.alert.AlertInstanceLogService;
import com.iviet.ivshs.service.alert.AlertInstanceService;
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
    private final AlertInstanceLogService AlertInstanceLogService;
    private final AlertConfigDao alertConfigDao;
    private final AlertInstanceDao alertInstanceDao;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void trigger(AlertTriggerRequestDto request) {
        AlertActionType actionType = request.getActionType();
        AlertInstance alert = null;
        String logMessage = request.getCustomMessage();
        AlertConfig config = request.getAlertConfig();

        if (actionType == AlertActionType.TRIGGERED) {
            if (config == null && request.getAlertConfigId() != null) {
                config = alertConfigDao.findById(request.getAlertConfigId())
                        .orElseThrow(() -> new NotFoundException("Cấu hình cảnh báo không tồn tại"));
            }

            if (config == null) {
                log.warn("Kích hoạt cảnh báo không thành công: Thiếu cấu hình cảnh báo.");
                return;
            }

            Optional<AlertInstance> latestOpen = alertInstanceDao.findLatestOpenByConfigId(config.getId());
            if (latestOpen.isPresent()) {
                AlertInstance existingAlert = latestOpen.get();
                if (config.getCooldownMinutes() > 0) {
                    Instant cooldownEnd = existingAlert.getTriggeredAt().plusSeconds(config.getCooldownMinutes() * 60L);
                    if (Instant.now().isBefore(cooldownEnd)) {
                        log.debug("Cảnh báo {} đang trong thời gian cooldown. Bỏ qua.", config.getId());
                        return;
                    }
                }
                alert = alertInstanceService.incrementTriggerCount(existingAlert.getId());
                actionType = AlertActionType.RE_TRIGGERED;
                logMessage = "Sự kiện tiếp diễn (Re-trigger) do sự cố vẫn chưa được giải quyết";
            } else {
                alert = alertInstanceService.createActiveAlert(config, request.getTemplateData());
                logMessage = "Sự cố bắt đầu — cấu hình [" + config.getAlertName() + "] kích hoạt";
            }
        } else {
            AlertStatus status = (actionType == AlertActionType.ACKNOWLEDGED) ? AlertStatus.ACKNOWLEDGED
                    : AlertStatus.RESOLVED;

            alert = alertInstanceService.updateStatus(request.getAlertInstanceId(), status, request.getActorType(),
                    request.getActorId());
            config = alert.getAlertConfig();

            if (logMessage == null) {
                logMessage = (actionType == AlertActionType.ACKNOWLEDGED) ? "Sự cố đã được xác nhận"
                        : "Sự cố đã được giải quyết";
            }
        }

        AlertInstanceLogService.createLog(alert, actionType, request.getActorType(), request.getActorId(), logMessage);
        eventPublisher.publishEvent(new AlertNotificationEvent(this, config, alert, actionType, request.getActorType(),
                request.getActorId()));
    }
}
