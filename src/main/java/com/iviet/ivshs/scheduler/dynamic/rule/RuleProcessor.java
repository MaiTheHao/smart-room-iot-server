package com.iviet.ivshs.scheduler.dynamic.rule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dao.ActionDao;
import com.iviet.ivshs.dao.AlertConfigDao;
import com.iviet.ivshs.dao.ConditionDao;
import com.iviet.ivshs.dao.RuleDao;
import com.iviet.ivshs.dto.ActionResult;
import com.iviet.ivshs.dto.AlertTriggerRequestDto;
import com.iviet.ivshs.dto.EvaluationResult;
import com.iviet.ivshs.entities.Action;
import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.entities.Rule;
import com.iviet.ivshs.scheduler.dynamic.base.JobProcessorType;
import com.iviet.ivshs.scheduler.dynamic.base.SchedulableJobProcessor;
import com.iviet.ivshs.service.AlertTriggerService;
import com.iviet.ivshs.service.strategy.ActionExecutionService;
import com.iviet.ivshs.service.strategy.ConditionEvaluationService;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.AlertActionType;
import com.iviet.ivshs.shared.enumeration.AlertActorType;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.enumeration.ConditionOwnerCategory;
import com.iviet.ivshs.shared.exception.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleProcessor implements SchedulableJobProcessor {

  private final RuleDao ruleDao;
  private final ConditionDao conditionDao;
  private final ActionDao actionDao;
  private final ConditionEvaluationService conditionEvaluationService;
  private final ActionExecutionService actionExecutionService;
  private final AlertConfigDao alertConfigDao;
  private final AlertTriggerService alertTriggerService;
  private final ObjectMapper objectMapper;

  @Override
  public JobProcessorType getProcessorType() {
    return JobProcessorType.RULE;
  }

  @Override
  @Transactional
  public void processJob(Long id) {
    Rule rule =
        ruleDao.findById(id).orElseThrow(() -> new NotFoundException("Rule not found: " + id));

    if (!Boolean.TRUE.equals(rule.getIsActive())) {
      return;
    }

    String ruleIdStr = String.valueOf(id);
    List<Condition> conditions = conditionDao.findByOwner(ConditionOwnerCategory.RULE, ruleIdStr);

    if (conditions.isEmpty()) {
      log.warn("Rule {} has no conditions", rule.getId());
      return;
    }

    log.info("Evaluating Rule {} with {} conditions", rule.getId(), conditions.size());

    EvaluationResult evalResult = conditionEvaluationService.evaluateAll(conditions, null);

    if (evalResult.isMatched()) {
      log.info("Rule {} condition matched. Executing actions...", rule.getId());

      List<Action> actions = actionDao.findByOwner(ActionOwnerCategory.RULE, ruleIdStr);
      List<ActionResult> actionResults = actionExecutionService.executeAll(actions);

      long successCount = actionResults.stream().filter(ActionResult::success).count();
      log.info(
          "Rule {} executed {}/{} actions successfully",
          rule.getId(),
          successCount,
          actionResults.size());

      triggerAlertsIfConfigured(rule, conditions, evalResult);
    }
  }

  private void triggerAlertsIfConfigured(
      Rule rule, List<Condition> conditions, EvaluationResult evalResult) {
    List<AlertConfig> alertConfigs = alertConfigDao.findAllByNamespaceAndSourceId(
        AlertNamespace.RULE, String.valueOf(rule.getId()));

    if (alertConfigs.isEmpty()) {
      return;
    }

    var templateData = new java.util.HashMap<>(evalResult.templateData());
    templateData.put("rule_name", rule.getName());
    templateData.put("rule_id", rule.getId());
    templateData.put("total_conditions", conditions.size());

    for (AlertConfig config : alertConfigs) {
      try {
        AlertTriggerRequestDto request = AlertTriggerRequestDto.builder()
            .alertConfig(config)
            .actionType(AlertActionType.TRIGGERED)
            .actorType(AlertActorType.RULE_ENGINE)
            .actorId(rule.getId().toString())
            .templateData(templateData)
            .payload(objectMapper.valueToTree(templateData))
            .build();
        alertTriggerService.trigger(request);
      } catch (Exception e) {
        log.error(
            "[Alert] Failed to trigger alert for config {}: {}", config.getId(), e.getMessage(), e);
      }
    }
  }
}
