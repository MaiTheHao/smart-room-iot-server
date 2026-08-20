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
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventHandler {

  private final RoomEventConfigDao roomEventConfigDao;
  private final ActionDao actionDao;
  private final AlertConfigDao alertConfigDao;
  private final ActionExecutionService actionExecutionService;
  private final AlertTriggerService alertTriggerService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleRoomEvent(RoomEventApplicationEvent event) {
    if (event == null || event.getRoomId() == null || event.getEventCode() == null) {
      return;
    }

    Optional<RoomEventConfig> configOpt =
        roomEventConfigDao.findByRoomIdAndEventCode(event.getRoomId(), event.getEventCode());
    if (configOpt.isEmpty()) {
      return;
    }

    RoomEventConfig config = configOpt.get();
    if (!Boolean.TRUE.equals(config.getIsActive())) {
      return;
    }

    if (config.getCooldownSeconds() != null
        && config.getCooldownSeconds() > 0
        && config.getLastTriggeredAt() != null
        && Instant.now().isBefore(config.getLastTriggeredAt().plusSeconds(config.getCooldownSeconds()))) {
      return;
    }

    processEventConfig(config, event);
  }

  private void processEventConfig(RoomEventConfig config, RoomEventApplicationEvent event) {
    config.setLastTriggeredAt(Instant.now());
    roomEventConfigDao.save(config);

    executeActions(config.getId());
    triggerAlerts(config.getId(), event);
  }

  private void executeActions(Long configId) {
    List<Action> actions =
        actionDao.findByOwner(ActionOwnerCategory.ROOM_EVENT, String.valueOf(configId));
    if (!actions.isEmpty()) {
      actionExecutionService.executeAll(actions);
    }
  }

  private void triggerAlerts(Long configId, RoomEventApplicationEvent event) {
    List<AlertConfig> alertConfigs = alertConfigDao.findAllByNamespaceAndSourceId(
        AlertNamespace.ROOM_EVENT, String.valueOf(configId));

    alertConfigs.forEach(config -> alertTriggerService.trigger(AlertTriggerRequestDto.builder()
        .alertConfig(config)
        .alertConfigId(config.getId())
        .actionType(AlertActionType.TRIGGERED)
        .actorType(AlertActorType.ROOM_EVENT)
        .actorId(event.getRoomId().toString())
        .templateData(Map.of(
            "room_id", event.getRoomId(),
            "event_code", event.getEventCode().name(),
            "timestamp", event.getEventTimestamp().toString()))
        .payload(event.getPayload())
        .build()));
  }
}
