package com.iviet.ivshs.service;

import com.iviet.ivshs.dao.ActionDao;
import com.iviet.ivshs.dao.AlertConfigDao;
import com.iviet.ivshs.dao.ConditionDao;
import com.iviet.ivshs.dao.RoomEventConfigDao;
import com.iviet.ivshs.dto.AlertTriggerRequestDto;
import com.iviet.ivshs.dto.EvaluationResult;
import com.iviet.ivshs.entities.Action;
import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.entities.RoomEventConfig;
import com.iviet.ivshs.event.RoomEventApplicationEvent;
import com.iviet.ivshs.service.strategy.ActionExecutionService;
import com.iviet.ivshs.service.strategy.ConditionEvaluationService;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.AlertActionType;
import com.iviet.ivshs.shared.enumeration.AlertActorType;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.enumeration.ConditionOwnerCategory;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventHandler {

  private final RoomEventConfigDao roomEventConfigDao;
  private final ActionDao actionDao;
  private final AlertConfigDao alertConfigDao;
  private final ConditionDao conditionDao;
  private final ActionExecutionService actionExecutionService;
  private final ConditionEvaluationService conditionEvaluationService;
  private final AlertTriggerService alertTriggerService;

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleRoomEvent(RoomEventApplicationEvent event) {
    if (event == null || event.getRoomId() == null || event.getEventCode() == null) {
      return;
    }

    final Instant now = Instant.now();

    Optional<RoomEventConfig> configOpt =
        roomEventConfigDao.findByRoomIdAndEventCode(event.getRoomId(), event.getEventCode());
    if (configOpt.isEmpty()) {
      return;
    }

    RoomEventConfig config = configOpt.get();
    if (!Boolean.TRUE.equals(config.getIsActive())) {
      return;
    }

    processEventConfig(config, event, now);
  }

  private void processEventConfig(RoomEventConfig config, RoomEventApplicationEvent event, Instant now) {
    int cooldownSeconds = config.getCooldownSeconds() != null ? config.getCooldownSeconds() : 0;

    if (cooldownSeconds > 0
        && config.getLastTriggeredAt() != null
        && now.isBefore(config.getLastTriggeredAt().plusSeconds(cooldownSeconds))) {
      return;
    }

    List<Condition> conditions = conditionDao.findByOwner(ConditionOwnerCategory.ROOM_EVENT, String.valueOf(config.getId()));
    
    if (!conditions.isEmpty()) {
      try {
        EvaluationResult evalResult = conditionEvaluationService.evaluateAll(conditions, config.getRoom().getId());
        if (!evalResult.isMatched()) {
          log.debug("Conditions not matched for RoomEventConfig id={}, skipping execution", config.getId());
          return;
        }
      } catch (Exception e) {
        log.error("Error evaluating conditions for RoomEventConfig id={}", config.getId(), e);
        return;
      }
    }

    Instant cooldownThreshold = cooldownSeconds > 0 ? now.minusSeconds(cooldownSeconds) : now;

    boolean acquired = roomEventConfigDao.tryUpdateLastTriggeredAt(config.getId(), now, cooldownThreshold);
    if (!acquired) {
      log.debug("RoomEventConfig id={} is in cooldown or recently triggered, skipping execution", config.getId());
      return;
    }

    executeActions(config.getId());
    triggerAlerts(config.getId(), event);
  }

  private void executeActions(Long configId) {
    try {
      List<Action> actions =
          actionDao.findByOwner(ActionOwnerCategory.ROOM_EVENT, String.valueOf(configId));
      if (!actions.isEmpty()) {
        actionExecutionService.executeAll(actions);
      }
    } catch (Exception e) {
      log.error("Error executing actions for RoomEventConfig id={}", configId, e);
    }
  }

  private void triggerAlerts(Long configId, RoomEventApplicationEvent event) {
    try {
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
    } catch (Exception e) {
      log.error("Error triggering alerts for RoomEventConfig id={}", configId, e);
    }
  }
}
