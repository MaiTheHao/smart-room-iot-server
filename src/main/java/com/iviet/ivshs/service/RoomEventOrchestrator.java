package com.iviet.ivshs.service;

import com.iviet.ivshs.dao.ActionDao;
import com.iviet.ivshs.dao.AlertConfigDao;
import com.iviet.ivshs.dao.RoomEventConfigDao;
import com.iviet.ivshs.dto.AlertTriggerRequestDto;
import com.iviet.ivshs.entities.Action;
import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.entities.RoomEventConfig;
import com.iviet.ivshs.event.RoomEventApplicationEvent;
import com.iviet.ivshs.service.strategy.ActionExecutionService;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.AlertActionType;
import com.iviet.ivshs.shared.enumeration.AlertActorType;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventOrchestrator {

    private final RoomEventConfigDao roomEventConfigDao;
    private final ActionDao actionDao;
    private final AlertConfigDao alertConfigDao;
    private final ActionExecutionService actionExecutionService;
    private final AlertTriggerService alertTriggerService;

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleRoomEvent(RoomEventApplicationEvent event) {
        if (!isValidEvent(event)) {
            return;
        }

        Optional<RoomEventConfig> configOpt = roomEventConfigDao.findByRoomIdAndEventCode(event.getRoomId(), event.getEventCode());
        if (configOpt.isEmpty()) {
            return;
        }

        RoomEventConfig config = configOpt.get();
        if (isInactiveOrCooldown(config)) {
            return;
        }

        markTriggered(config);
        executeConfiguredActions(config.getId());
        triggerConfiguredAlerts(config.getId(), event);
    }

    private boolean isValidEvent(RoomEventApplicationEvent event) {
        return event != null && event.getRoomId() != null && event.getEventCode() != null;
    }

    private boolean isInactiveOrCooldown(RoomEventConfig config) {
        if (Boolean.FALSE.equals(config.getIsActive())) {
            return true;
        }
        return isInCooldown(config, Instant.now());
    }

    private boolean isInCooldown(RoomEventConfig config, Instant now) {
        if (config.getCooldownSeconds() == null || config.getCooldownSeconds() <= 0 || config.getLastTriggeredAt() == null) {
            return false;
        }
        Instant cooldownEnd = config.getLastTriggeredAt().plusSeconds(config.getCooldownSeconds());
        return now.isBefore(cooldownEnd);
    }

    private void markTriggered(RoomEventConfig config) {
        config.setLastTriggeredAt(Instant.now());
        roomEventConfigDao.save(config);
    }

    private void executeConfiguredActions(Long configId) {
        String ownerId = String.valueOf(configId);
        List<Action> actions = actionDao.findByOwner(ActionOwnerCategory.ROOM_EVENT, ownerId);
        if (!actions.isEmpty()) {
            actionExecutionService.executeAll(actions);
        }
    }

    private void triggerConfiguredAlerts(Long configId, RoomEventApplicationEvent event) {
        String ownerId = String.valueOf(configId);
        List<AlertConfig> alertConfigs = alertConfigDao.findAllByNamespaceAndSourceId(AlertNamespace.ROOM_EVENT, ownerId);
        for (AlertConfig alertConfig : alertConfigs) {
            triggerSingleAlert(alertConfig, event);
        }
    }

    private void triggerSingleAlert(AlertConfig alertConfig, RoomEventApplicationEvent event) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("roomId", event.getRoomId());
        templateData.put("eventCode", event.getEventCode().name());
        templateData.put("timestamp", event.getEventTimestamp().toString());

        AlertTriggerRequestDto request = AlertTriggerRequestDto.builder()
                .alertConfig(alertConfig)
                .alertConfigId(alertConfig.getId())
                .actionType(AlertActionType.TRIGGERED)
                .actorType(AlertActorType.SYSTEM)
                .actorId("ROOM_EVENT_ORCHESTRATOR")
                .templateData(templateData)
                .payload(event.getPayload())
                .build();

        alertTriggerService.trigger(request);
    }
}
