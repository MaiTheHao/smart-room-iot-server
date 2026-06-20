package com.iviet.ivshs.scheduler.dynamic.rule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.entities.RuleAction;
import com.iviet.ivshs.entities.RuleActionAlert;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.scheduler.dynamic.rule.strategy.RuleDataSourceStrategy;
import com.iviet.ivshs.service.control.DeviceControlServiceStrategy;
import com.iviet.ivshs.core.properties.EngineProperties;
import com.iviet.ivshs.entities.Rule;
import com.iviet.ivshs.shared.enumeration.ConditionLogic;
import com.iviet.ivshs.shared.enumeration.ConditionOperator;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.scheduler.dynamic.base.SchedulableJobProcessor;
import com.iviet.ivshs.scheduler.dynamic.base.JobProcessorType;
import com.iviet.ivshs.dao.RuleDao;
import com.iviet.ivshs.service.alert.AlertService;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleProcessor implements SchedulableJobProcessor {

    private final EngineProperties engineProperties;
    private final List<RuleDataSourceStrategy> ruleDataSourceStrategies;
    private final List<DeviceControlServiceStrategy<?>> controlStrategies;
    private final ObjectMapper objectMapper;
    private final RuleDao ruleDao;
    private final AlertService alertService;

    private Map<DeviceCategory, DeviceControlServiceStrategy<?>> strategyMap;

    @PostConstruct
    private void init() {
        strategyMap = controlStrategies.stream().collect(Collectors.toUnmodifiableMap(DeviceControlServiceStrategy::getSupportedCategory, Function.identity()));
        log.info("RuleProcessor initialized with epsilon = {} and categories: {}", engineProperties.getRuleComputeEpsilon(), strategyMap.keySet());
    }

    @Override
    public JobProcessorType getProcessorType() {
        return JobProcessorType.RULE;
    }

    @Override
    @Transactional
    public void processJob(Long id) {
        Rule rule = ruleDao.findByIdWithConditionsAndActions(id)
            .orElseThrow(() -> new NotFoundException("Rule not found: " + id));
        if (Boolean.FALSE.equals(rule.getIsActive())) return;
        this.process(rule);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void process(Rule rule) {
        if (!Boolean.TRUE.equals(rule.getIsActive()))
            return;

        List<RuleCondition> conditions = rule.getConditions();
        if (conditions == null || conditions.isEmpty()) {
            log.warn("Rule {} has no conditions", rule.getId());
            return;
        }

        log.info("Evaluating Rule {} with {} conditions", rule.getId(), conditions.size());

        EvaluationContext initCtx = new EvaluationContext();

        boolean isMatched = conditions.stream().sorted(Comparator.comparingInt(RuleCondition::getSortOrder)).reduce(initCtx, this::accumulateResult, (a, b) -> a).isFinalResult();

        List<RuleActionAlert> alertConfigs = rule.getAlerts();
        if (isMatched) {
            executeActions(rule);

            if (alertConfigs != null) {
                for (RuleActionAlert config : alertConfigs) {
                    try {
                        alertService.triggerAlert(config.getId());
                    } catch (Exception e) {
                        log.error("[Alert] Failed to trigger alert for config {}: {}", config.getId(), e.getMessage(), e);
                    }
                }
            }
        } else {
            if (alertConfigs != null) {
                for (RuleActionAlert config : alertConfigs) {
                    try {
                        alertService.resolveAlertIfNeeded(config.getId());
                    } catch (Exception e) {
                        log.error("[Alert] Failed to auto-resolve alert for config {}: {}", config.getId(), e.getMessage(), e);
                    }
                }
            }
        }
    }

    private EvaluationContext accumulateResult(EvaluationContext ctx, RuleCondition cond) {
        RuleCondition tempCond = new RuleCondition();
        tempCond.setDataSource(cond.getDataSource());
        tempCond.setResourceParam(cond.getResourceParam());
        tempCond.setOperator(cond.getOperator());
        tempCond.setValue(cond.getValue());

        boolean isMet = evaluateCondition(tempCond);

        if (ctx.isFirst()) {
            ctx.setFinalResult(isMet);
            ctx.setFirst(false);
        } else {
            ConditionLogic logic = ctx.getPrevLogic();
            boolean newResult = (logic == ConditionLogic.OR) ? ctx.isFinalResult() || isMet : ctx.isFinalResult() && isMet;
            ctx.setFinalResult(newResult);
        }

        ctx.setPrevLogic(cond.getNextLogic());
        return ctx;
    }

    private boolean evaluateCondition(RuleCondition cond) {
        return ruleDataSourceStrategies.stream().filter(s -> s.supports(cond.getDataSource())).findFirst().map(strategy -> {
            try {
                Object val = strategy.fetchValue(cond, null);
                return val != null && compareValues(val.toString(), cond.getValue(), cond.getOperator());
            } catch (Exception e) {
                log.error("Error in strategy {}: {}", strategy.getClass().getSimpleName(), e.getMessage());
                return false;
            }
        }).orElseGet(() -> {
            log.warn("No strategy for: {}", cond.getDataSource());
            return false;
        });
    }

    private boolean compareValues(String actual, String target, ConditionOperator op) {
        try {
            double v1 = Double.parseDouble(actual);
            double v2 = Double.parseDouble(target);

            boolean res = switch (op) {
                case GT -> v1 > v2;
                case LT -> v1 < v2;
                case EQ -> Math.abs(v1 - v2) < engineProperties.getRuleComputeEpsilon();
                case NEQ -> Math.abs(v1 - v2) >= engineProperties.getRuleComputeEpsilon();
                case GTE -> v1 >= v2;
                case LTE -> v1 <= v2;
                default -> false;
            };
            log.debug("Numeric: {} {} {} -> {}", v1, op.getSymbol(), v2, res);
            return res;
        } catch (NumberFormatException e) {
            boolean res = switch (op) {
                case GT -> actual.compareToIgnoreCase(target) > 0;
                case LT -> actual.compareToIgnoreCase(target) < 0;
                case EQ -> actual.equals(target);
                case NEQ -> !actual.equals(target);
                case GTE -> actual.compareToIgnoreCase(target) >= 0;
                case LTE -> actual.compareToIgnoreCase(target) <= 0;
                default -> {
                    log.warn("Invalid operator for non-numeric: {}", op);
                    yield false;
                }
            };
            log.debug("String: '{}' {} '{}' -> {}", actual, op.getSymbol(), target, res);
            return res;
        }
    }

    @SuppressWarnings({
            "unchecked",
            "rawtypes"
    })
    private void executeActions(Rule rule) {
        List<RuleAction> actions = rule.getActions();
        if (actions == null || actions.isEmpty())
            return;

        actions.stream().sorted(Comparator.comparingInt(a -> a.getExecutionOrder() != null ? a.getExecutionOrder() : 0)).forEach(action -> {
            DeviceControlServiceStrategy strategy = strategyMap.get(action.getTargetDeviceCategory());
            if (strategy == null) {
                log.error("Missing strategy for category: {}", action.getTargetDeviceCategory());
                return;
            }

            try {
                Object controlDto = objectMapper.treeToValue(action.getActionParams(), strategy.getControlDtoClass());
                strategy.control(action.getTargetDeviceId(), controlDto);
                log.info("Executed Rule [{}] action for device [{}]", rule.getName(), action.getTargetDeviceId());
            } catch (Exception e) {
                log.error("Failed to execute Rule {} action {}: {}", rule.getId(), action.getId(), e.getMessage());
            }
        });
    }

    @Data
    private static class EvaluationContext {
        private boolean finalResult = true;
        private boolean isFirst = true;
        private ConditionLogic prevLogic;
    }
}
