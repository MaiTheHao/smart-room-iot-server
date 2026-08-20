package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dto.ActionResult;
import com.iviet.ivshs.dto.ControlDeviceResult;
import com.iviet.ivshs.entities.Action;
import com.iviet.ivshs.service.registry.DeviceControlStrategyRegistry;
import com.iviet.ivshs.service.strategy.ActionExecutionService;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionExecutionServiceImpl implements ActionExecutionService {

  private final DeviceControlStrategyRegistry deviceControlStrategyRegistry;

  @Override
  public ControlDeviceResult execute(Action action) {
    if (action == null) {
      return null;
    }

    DeviceCategory category = action.getTargetCategory();
    String targetId = action.getTargetId();
    JsonNode params = action.getParams();

    if (category == null || targetId == null || params == null) {
      log.debug(
          "Category, targetId, or params is null: category={}, targetId={}", category, targetId);
      return null;
    }

    try {
      Long id = Long.parseLong(targetId);
      return deviceControlStrategyRegistry.executeControl(category, id, params);
    } catch (NumberFormatException e) {
      return deviceControlStrategyRegistry.executeControl(category, targetId, params);
    }
  }

  @Override
  public List<ActionResult> executeAll(List<Action> actions) {
    if (actions == null || actions.isEmpty()) {
      return Collections.emptyList();
    }

    List<Action> sortedActions = sortByExecutionOrder(actions);

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<Future<ActionResult>> futures = submitActions(sortedActions, executor);
      return awaitAll(futures);
    }
  }

  private List<Action> sortByExecutionOrder(List<Action> actions) {
    return actions.stream()
        .sorted(
            Comparator.comparingInt(a -> a.getExecutionOrder() != null ? a.getExecutionOrder() : 0))
        .toList();
  }

  private List<Future<ActionResult>> submitActions(List<Action> actions, ExecutorService executor) {
    return actions.stream()
        .map(action -> executor.submit(() -> runSingleAction(action)))
        .toList();
  }

  private List<ActionResult> awaitAll(List<Future<ActionResult>> futures) {
    return futures.stream().map(this::resolveResult).toList();
  }

  private ActionResult resolveResult(Future<ActionResult> future) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return ActionResult.failure(null, null, null, "Execution interrupted: " + e.getMessage());
    } catch (ExecutionException e) {
      Throwable cause = e.getCause() != null ? e.getCause() : e;
      return ActionResult.failure(null, null, null, "Execution failed: " + cause.getMessage());
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
          "Failed to execute action id={}, category={}, targetId={}: {}",
          action.getId(),
          action.getTargetCategory(),
          action.getTargetId(),
          e.getMessage(),
          e);
      return ActionResult.failure(
          action.getId(), action.getTargetCategory(), action.getTargetId(), e.getMessage());
    }
  }
}
