package com.iviet.ivshs.schedule.rule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.entities.RuleActionV2;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.entities.RuleConditionV2;
import com.iviet.ivshs.entities.RuleV2;
import com.iviet.ivshs.enumeration.ConditionLogic;
import com.iviet.ivshs.enumeration.ConditionOperator;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.schedule.rule.strategy.RuleDataSourceStrategy;
import com.iviet.ivshs.service.strategy.DeviceControlServiceStrategy;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j(topic = "RULEV2-PROCESSOR")
@Component
@RequiredArgsConstructor
public class RuleV2Processor {

    private final Environment env;
    private final List<RuleDataSourceStrategy> ruleDataSourceStrategies;
    private final List<DeviceControlServiceStrategy<?>> controlStrategies;
    private final ObjectMapper objectMapper;

    private static final double DEFAULT_EPSILON = 0.05;
    private double EPSILON = DEFAULT_EPSILON;
    private Map<DeviceCategory, DeviceControlServiceStrategy<?>> strategyMap;

    @PostConstruct 
    private void init() {
        Double epsilonConfig = env.getProperty("app.engine.rule.computeEpsilon", Double.class);
        if (epsilonConfig != null) EPSILON = epsilonConfig;
        
        strategyMap = controlStrategies.stream()
                .collect(Collectors.toUnmodifiableMap(
                        DeviceControlServiceStrategy::getSupportedCategory,
                        Function.identity()
                ));
        log.info("RuleV2Processor initialized with epsilon = {} and categories: {}", EPSILON, strategyMap.keySet());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void process(RuleV2 rule) {
        if (!Boolean.TRUE.equals(rule.getIsActive())) return;

        List<RuleConditionV2> conditions = rule.getConditions();
        if (conditions == null || conditions.isEmpty()) {
            log.warn("RuleV2 {} has no conditions", rule.getId());
            return;
        }

        log.info("Evaluating RuleV2 {} with {} conditions", rule.getId(), conditions.size());

        EvaluationContext initCtx = new EvaluationContext();
        
        boolean isMatched = conditions.stream()
                .sorted(Comparator.comparingInt(RuleConditionV2::getSortOrder))
                .reduce(initCtx, this::accumulateResult, (a, b) -> a)
                .isFinalResult();

        if (isMatched) {
            executeActions(rule);
        }
    }

    private EvaluationContext accumulateResult(EvaluationContext ctx, RuleConditionV2 condV2) {
        RuleCondition tempCond = new RuleCondition();
        tempCond.setDataSource(condV2.getDataSource());
        tempCond.setResourceParam(condV2.getResourceParam());
        tempCond.setOperator(condV2.getOperator());
        tempCond.setValue(condV2.getValue());

        boolean isMet = evaluateCondition(tempCond);
        
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
        
        ctx.setPrevLogic(condV2.getNextLogic());
        return ctx;
    }

    private boolean evaluateCondition(RuleCondition cond) {
        return ruleDataSourceStrategies.stream()
                .filter(s -> s.supports(cond.getDataSource()))
                .findFirst()
                .map(strategy -> {
                    try {
                        Object val = strategy.fetchValue(cond, null);
                        return val != null && compareValues(val.toString(), cond.getValue(), cond.getOperator());
                    } catch (Exception e) {
                        log.error("Error in strategy {}: {}", strategy.getClass().getSimpleName(), e.getMessage());
                        return false;
                    }
                })
                .orElseGet(() -> {
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
                case EQ -> Math.abs(v1 - v2) < EPSILON;
                case NEQ -> Math.abs(v1 - v2) >= EPSILON;
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void executeActions(RuleV2 rule) {
        List<RuleActionV2> actions = rule.getActions();
        if (actions == null || actions.isEmpty()) return;

        actions.stream()
            .sorted(Comparator.comparingInt(a -> a.getExecutionOrder() != null ? a.getExecutionOrder() : 0))
            .forEach(action -> {
                DeviceControlServiceStrategy strategy = strategyMap.get(action.getTargetDeviceCategory());
                if (strategy == null) {
                    log.error("Missing strategy for category: {}", action.getTargetDeviceCategory());
                    return;
                }

                try {
                    Object controlDto = objectMapper.treeToValue(action.getActionParams(), strategy.getControlDtoClass());
                    strategy.control(action.getTargetDeviceId(), controlDto);
                    log.info("Executed RuleV2 [{}] action for device [{}]", rule.getName(), action.getTargetDeviceId());
                } catch (Exception e) {
                    log.error("Failed to execute RuleV2 {} action {}: {}", rule.getId(), action.getId(), e.getMessage());
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
