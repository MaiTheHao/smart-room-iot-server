package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.dto.ActionResult;
import com.iviet.ivshs.dto.ControlDeviceResult;
import com.iviet.ivshs.entities.Action;
import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.service.registry.DeviceControlStrategyRegistry;
import com.iviet.ivshs.service.strategy.ActionExecutionService;
import com.iviet.ivshs.service.strategy.DeviceControlServiceStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.DeviceSpecificType;
import com.iviet.ivshs.shared.exception.BadRequestException;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.shared.util.DeviceCapabilityRegistry;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionExecutionServiceImpl implements ActionExecutionService {

  private final DeviceControlStrategyRegistry deviceControlStrategyRegistry;
  private final ObjectMapper objectMapper;
  private final Validator validator;
  private final FanDao fanDao;
  private final LightDao lightDao;
  private final AirConditionDao airConditionDao;

  @Override
  public ControlDeviceResult execute(Action action) {
    if (action == null) {
      throw new BadRequestException("Action cannot be null");
    }

    DeviceCategory category = action.getTargetCategory();
    String targetId = action.getTargetId();
    JsonNode params = action.getParams();

    if (category == null || targetId == null) {
      throw new BadRequestException("Action targetCategory and targetId must not be null");
    }

    try {
      Long deviceId = Long.valueOf(targetId);
      return deviceControlStrategyRegistry.executeControl(category, deviceId, params);
    } catch (NumberFormatException e) {
      return deviceControlStrategyRegistry.executeControl(category, targetId, params);
    }
  }

  @Override
  public List<ActionResult> executeAll(List<Action> actions) {
    if (actions == null || actions.isEmpty()) {
      return Collections.emptyList();
    }

    List<Action> sortedActions = actions.stream()
        .sorted(
            Comparator.comparingInt(a -> a.getExecutionOrder() != null ? a.getExecutionOrder() : 0))
        .toList();

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<Future<ActionResult>> futures = sortedActions.stream()
          .map(action -> executor.submit(() -> runSingleAction(action)))
          .toList();

      return futures.stream()
          .map(future -> {
            try {
              return future.get();
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              return ActionResult.failure(
                  null, null, null, "Execution interrupted: " + e.getMessage());
            } catch (ExecutionException e) {
              Throwable cause = e.getCause() != null ? e.getCause() : e;
              return ActionResult.failure(
                  null, null, null, "Execution failed: " + cause.getMessage());
            }
          })
          .toList();
    }
  }

  private ActionResult runSingleAction(Action action) {
    try {
      ControlDeviceResult result = execute(action);
      log.info(
          "Action executed successfully: actionId={}, category={}, targetId={}",
          action.getId(),
          action.getTargetCategory(),
          action.getTargetId());
      return ActionResult.success(
          action.getId(), action.getTargetCategory(), action.getTargetId(), result);
    } catch (Exception e) {
      log.error(
          "Action failed: actionId={}, category={}, targetId={}, error={}",
          action.getId(),
          action.getTargetCategory(),
          action.getTargetId(),
          e.getMessage(),
          e);
      return ActionResult.failure(
          action.getId(), action.getTargetCategory(), action.getTargetId(), e.getMessage());
    }
  }

  @Override
  public void validateActionParams(DeviceCategory category, Long targetDeviceId, JsonNode params) {
    DeviceControlServiceStrategy<?> strategy = deviceControlStrategyRegistry.getStrategy(category);

    DeviceSpecificType specificType = getSpecificType(category, targetDeviceId);

    validateCapabilities(category, specificType, params);
    validateDtoConstraints(params, strategy.getControlDtoClass());
  }

  private DeviceSpecificType getSpecificType(DeviceCategory category, Long targetDeviceId) {
    return switch (category) {
      case null -> DeviceSpecificType.GPIO;
      case FAN ->
        fanDao
            .findById(targetDeviceId)
            .map(Fan::getSpecificType)
            .orElseThrow(() -> new NotFoundException("Fan not found with id: " + targetDeviceId));
      case LIGHT ->
        lightDao
            .findById(targetDeviceId)
            .map(Light::getSpecificType)
            .orElseThrow(() -> new NotFoundException("Light not found with id: " + targetDeviceId));
      case AIR_CONDITION ->
        airConditionDao
            .findById(targetDeviceId)
            .map(AirCondition::getSpecificType)
            .orElseThrow(
                () -> new NotFoundException("AirCondition not found with id: " + targetDeviceId));
      default -> DeviceSpecificType.GPIO;
    };
  }

  private void validateCapabilities(
      DeviceCategory category, DeviceSpecificType specificType, JsonNode params) {
    if (params == null || !params.isObject()) {
      return;
    }

    params.fields().forEachRemaining(entry -> {
      String field = entry.getKey();
      JsonNode valNode = entry.getValue();
      if (valNode != null && !valNode.isNull()) {
        if (!DeviceCapabilityRegistry.isSupported(category, specificType, field)) {
          throw new BadRequestException("Device category "
              + category
              + " with type "
              + specificType
              + " does not support parameter: "
              + field);
        }
      }
    });
  }

  private void validateDtoConstraints(JsonNode params, Class<?> dtoClass) {
    try {
      Object dto = objectMapper.treeToValue(params, dtoClass);
      var violations = validator.validate(dto);
      if (!violations.isEmpty()) {
        String errorMsg = violations.stream()
            .map(v -> v.getPropertyPath() + " " + v.getMessage())
            .collect(Collectors.joining(", "));
        throw new BadRequestException(errorMsg);
      }
    } catch (JsonProcessingException e) {
      throw new BadRequestException("Invalid JSON format for action parameters");
    }
  }
}
