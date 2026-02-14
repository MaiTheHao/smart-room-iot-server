package com.iviet.ivshs.rule;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.entities.Rule;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.rule.strategy.RuleDataSourceStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleEvaluator {

    private final List<RuleDataSourceStrategy> ruleDataSourceStrategies;

    private static final String LOGIC_OR = "OR";
    private static final double EPSILON = .001;

    public boolean matches(Rule rule) {
        if (rule.getConditions() == null || rule.getConditions().isEmpty()) {
            return false;
        }

        List<RuleCondition> conditions = rule.getConditions();
        conditions.sort(Comparator.comparingInt(RuleCondition::getSortOrder));

        boolean currentResult = true;

        for (int i = 0; i < conditions.size(); i++) {
            RuleCondition cond = conditions.get(i);
            boolean isMet = evaluateCondition(cond, rule.getRoomId());
            
            if (i == 0) {
                currentResult = isMet;
            } else {
                RuleCondition prev = conditions.get(i - 1);
                String logic = prev.getNextLogic();

                if (LOGIC_OR.equalsIgnoreCase(logic)) {
                    currentResult = currentResult || isMet;
                } else {
                    currentResult = currentResult && isMet;
                }
            }
        }

        return currentResult;
    }

    private boolean evaluateCondition(RuleCondition cond, Long contextId) {
        for (RuleDataSourceStrategy strategy : ruleDataSourceStrategies) {
            if (strategy.supports(cond)) {
                return processStrategy(strategy, cond, contextId);
            }
        }
        return false;
    }

    private boolean processStrategy(RuleDataSourceStrategy strategy, RuleCondition cond, Long contextId) {
        try {
            Object valueObj = strategy.provide(cond, contextId);
            if (valueObj == null) {
                return false;
            }
            return compareValues(valueObj.toString(), cond.getValue(), cond.getOperator());
        } catch (Exception e) {
            log.warn("Failed to evaluate condition {}: {}", cond.getId(), e.getMessage());
            return false;
        }
    }

    private boolean compareValues(String actualStr, String targetStr, String operator) {
        try {
            double actualValue = Double.parseDouble(actualStr);
            double targetValue = Double.parseDouble(targetStr);
            return compareNumeric(actualValue, targetValue, operator);
        } catch (NumberFormatException e) {
            return compareString(actualStr, targetStr, operator);
        }
    }

    private boolean compareNumeric(double actual, double target, String operator) {
        return switch (operator) {
            case ">" -> actual > target;
            case "<" -> actual < target;
            case "=" -> Math.abs(actual - target) < EPSILON;
            case "!=" -> Math.abs(actual - target) >= EPSILON;
            case ">=" -> actual >= target;
            case "<=" -> actual <= target;
            default -> false;
        };
    }

    private boolean compareString(String actual, String target, String operator) {
        return switch (operator) {
            case "=" -> actual.equalsIgnoreCase(target);
            case "!=" -> !actual.equalsIgnoreCase(target);
            default -> false;
        };
    }
}
