package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.core.properties.EngineProperties;
import com.iviet.ivshs.dto.ConditionValue;
import com.iviet.ivshs.dto.EvaluationResult;
import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.service.registry.ConditionDataSourceRegistry;
import com.iviet.ivshs.service.strategy.ConditionEvaluationService;
import com.iviet.ivshs.shared.enumeration.ConditionLogic;
import com.iviet.ivshs.shared.enumeration.ConditionOperator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConditionEvaluationServiceImpl implements ConditionEvaluationService {

  private final ConditionDataSourceRegistry conditionDataSourceRegistry;
  private final EngineProperties engineProperties;

  @Override
  public boolean evaluate(Condition condition, Long contextId) {
    if (condition == null) {
      return false;
    }
    long startTime = System.currentTimeMillis();
    try {
      ConditionValue conditionValue = conditionDataSourceRegistry.fetchValue(condition, contextId);
      Object actualRaw =
          switch (conditionValue) {
            case ConditionValue.NumericValue nv -> nv.value();
            case ConditionValue.TextValue tv -> tv.value();
            case ConditionValue.MissingValue mv -> null;
          };

      boolean isMet =
          switch (conditionValue) {
            case ConditionValue.NumericValue nv ->
              compareNumeric(nv.value(), condition.getValue(), condition.getOperator());
            case ConditionValue.TextValue tv ->
              compareText(tv.value(), condition.getValue(), condition.getOperator());
            case ConditionValue.MissingValue mv -> false;
          };

      long duration = System.currentTimeMillis() - startTime;
      log.debug(
          "[ConditionEvaluation] Single condition evaluated: conditionId={}, contextId={},"
              + " category={}, actual='{}', op='{}', expected='{}' => isMet={}, duration={}ms",
          condition.getId(),
          contextId,
          condition.getSourceCategory(),
          actualRaw,
          condition.getOperator() != null ? condition.getOperator().getSymbol() : "null",
          condition.getValue(),
          isMet,
          duration);

      return isMet;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error(
          "[ConditionEvaluation] Error evaluating single condition: conditionId={}, contextId={},"
              + " category={}, duration={}ms: {}",
          condition.getId(),
          contextId,
          condition.getSourceCategory(),
          duration,
          e.getMessage(),
          e);
      return false;
    }
  }

  @Override
  public EvaluationResult evaluateAll(List<Condition> conditions, Long contextId) {
    if (conditions == null || conditions.isEmpty()) {
      return EvaluationResult.empty();
    }

    long startBatchTime = System.currentTimeMillis();
    log.info(
        "[ConditionEvaluation] Batch condition evaluation started: contextId={},"
            + " totalConditions={}",
        contextId,
        conditions.size());

    EvaluationAccumulator ctx = new EvaluationAccumulator();

    List<Condition> sortedConditions = conditions.stream()
        .sorted(Comparator.comparingInt(c -> c.getSortOrder() != null ? c.getSortOrder() : 0))
        .toList();

    for (Condition cond : sortedConditions) {
      accumulate(ctx, cond, contextId);
    }

    long durationBatch = System.currentTimeMillis() - startBatchTime;
    log.info(
        "[ConditionEvaluation] Batch condition evaluation completed: contextId={}, finalResult={},"
            + " duration={}ms",
        contextId,
        ctx.isFinalResult(),
        durationBatch);

    return EvaluationResult.of(ctx.isFinalResult(), ctx.getTemplateData());
  }

  private void accumulate(EvaluationAccumulator ctx, Condition cond, Long contextId) {
    Object actualRaw = null;
    boolean isMet = false;
    long startTime = System.currentTimeMillis();

    try {
      ConditionValue conditionValue = conditionDataSourceRegistry.fetchValue(cond, contextId);
      actualRaw = switch (conditionValue) {
        case ConditionValue.NumericValue nv -> nv.value();
        case ConditionValue.TextValue tv -> tv.value();
        case ConditionValue.MissingValue mv -> null;
      };

      isMet = switch (conditionValue) {
        case ConditionValue.NumericValue nv ->
          compareNumeric(nv.value(), cond.getValue(), cond.getOperator());
        case ConditionValue.TextValue tv ->
          compareText(tv.value(), cond.getValue(), cond.getOperator());
        case ConditionValue.MissingValue mv -> false;
      };

      long duration = System.currentTimeMillis() - startTime;
      log.debug(
          "[ConditionEvaluation] Condition evaluated: conditionId={}, sortOrder={}, category={},"
              + " actual='{}', op='{}', expected='{}' => isMet={}, duration={}ms",
          cond.getId(),
          cond.getSortOrder(),
          cond.getSourceCategory(),
          actualRaw,
          cond.getOperator() != null ? cond.getOperator().getSymbol() : "null",
          cond.getValue(),
          isMet,
          duration);

    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error(
          "[ConditionEvaluation] Error evaluating condition: conditionId={}, sortOrder={},"
              + " category={}, duration={}ms: {}",
          cond.getId(),
          cond.getSortOrder(),
          cond.getSourceCategory(),
          duration,
          e.getMessage(),
          e);
      isMet = false;
    }

    if (ctx.isFirst()) {
      ctx.setFinalResult(isMet);
      ctx.setFirst(false);
    } else {
      ConditionLogic logic = ctx.getPrevLogic();
      boolean newResult = (logic == ConditionLogic.OR)
          ? ctx.isFinalResult() || isMet
          : ctx.isFinalResult() && isMet;
      ctx.setFinalResult(newResult);
    }

    int sortOrder = cond.getSortOrder() != null ? cond.getSortOrder() : 0;
    ctx.getTemplateData().put("cond" + sortOrder + "_value", actualRaw);
    ctx.getTemplateData().put("cond" + sortOrder + "_threshold", cond.getValue());
    ctx.getTemplateData()
        .put(
            "cond" + sortOrder + "_operator",
            cond.getOperator() != null ? cond.getOperator().getSymbol() : "");

    ctx.setPrevLogic(cond.getNextLogic());
  }

  private boolean compareNumeric(double actual, String targetStr, ConditionOperator op) {
    if (op == null || targetStr == null) {
      return false;
    }
    try {
      double target = Double.parseDouble(targetStr);
      double epsilon = engineProperties != null ? engineProperties.getRuleComputeEpsilon() : 0.0001;

      return switch (op) {
        case GT -> actual > target;
        case LT -> actual < target;
        case EQ -> Math.abs(actual - target) < epsilon;
        case NEQ -> Math.abs(actual - target) >= epsilon;
        case GTE -> actual >= target;
        case LTE -> actual <= target;
      };
    } catch (NumberFormatException e) {
      return compareText(String.valueOf(actual), targetStr, op);
    }
  }

  private boolean compareText(String actual, String target, ConditionOperator op) {
    if (actual == null || target == null || op == null) {
      return false;
    }
    return switch (op) {
      case GT -> actual.compareToIgnoreCase(target) > 0;
      case LT -> actual.compareToIgnoreCase(target) < 0;
      case EQ -> actual.equalsIgnoreCase(target);
      case NEQ -> !actual.equalsIgnoreCase(target);
      case GTE -> actual.compareToIgnoreCase(target) >= 0;
      case LTE -> actual.compareToIgnoreCase(target) <= 0;
    };
  }

  @Data
  private static class EvaluationAccumulator {
    private boolean finalResult = true;
    private boolean isFirst = true;
    private ConditionLogic prevLogic;
    private Map<String, Object> templateData = new HashMap<>();
  }
}
