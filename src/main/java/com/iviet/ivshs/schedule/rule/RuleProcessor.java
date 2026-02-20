package com.iviet.ivshs.schedule.rule;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.entities.Rule;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.schedule.rule.strategy.RuleDataSourceStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "RULE_PROCESSOR")
@Component
@RequiredArgsConstructor
public class RuleProcessor {

	private final List<RuleDataSourceStrategy> ruleDataSourceStrategies;

	private static final String LOGIC_OR = "OR";
	private static final double EPSILON = .001;

	public boolean matches(Rule rule) {
		if (rule.getConditions() == null || rule.getConditions().isEmpty()) {
			log.warn("Rule {} has no conditions", rule.getId());
			return false;
		}

		List<RuleCondition> conditions = rule.getConditions();
		sortConditions(conditions);

		log.info("Evaluating rule {} with {} conditions", rule.getId(), conditions.size());

		boolean currentResult = true;

		for (int i = 0; i < conditions.size(); i++) {
			RuleCondition cond = conditions.get(i);
			log.debug("Evaluating condition {} (operator: {}, value: {})", cond.getId(), cond.getOperator(), cond.getValue());
			boolean isMet = evaluateCondition(cond, rule.getRoomId());

			log.info("Condition {} evaluated to {}", cond.getId(), isMet);

			if (i == 0) {
				currentResult = isMet;
			} else {
				RuleCondition prev = conditions.get(i - 1);
				String logic = prev.getNextLogic();
				log.debug("Applying logic {} between previous and current condition", logic);

				if (LOGIC_OR.equalsIgnoreCase(logic)) {
					currentResult = currentResult || isMet;
				} else {
					currentResult = currentResult && isMet;
				}
			}
		}

		log.info("Final result for rule {}: {}", rule.getId(), currentResult);
		return currentResult;
	}

	private void sortConditions(List<RuleCondition> conditions) {
		conditions.sort(Comparator.comparingInt(RuleCondition::getSortOrder));
	}

	private boolean evaluateCondition(RuleCondition cond, Long contextId) {
		for (RuleDataSourceStrategy strategy : ruleDataSourceStrategies) {
			if (strategy.supports(cond.getDataSource())) {
				try {
					log.debug("Using Data Source Strategy {} for condition {}", strategy.getClass().getSimpleName(), cond.getId());

					Object valueObj = strategy.fetchValue(cond, contextId);

					if (valueObj == null) {
						log.warn("Strategy {} returned null for condition {}", strategy.getClass().getSimpleName(), cond.getId());
						return false;
					}
					return compareValues(valueObj.toString(), cond.getValue(), cond.getOperator());

				} catch (Exception e) {
					log.warn("Failed to evaluate condition {}: {}", cond.getId(), e.getMessage());
					return false;
				}
			}
		}

		log.warn("No strategy found for data source: {}", cond.getDataSource());
		return false;
	}

	private boolean compareValues(String actualStr, String targetStr, String operator) {
		try {
			double actualValue = Double.parseDouble(actualStr);
			double targetValue = Double.parseDouble(targetStr);
			boolean result = switch (operator) {
				case ">" -> actualValue > targetValue;
				case "<" -> actualValue < targetValue;
				case "=" -> Math.abs(actualValue - targetValue) < EPSILON;
				case "!=" -> Math.abs(actualValue - targetValue) >= EPSILON;
				case ">=" -> actualValue >= targetValue;
				case "<=" -> actualValue <= targetValue;
				default -> false;
			};
			log.debug("Numeric comparison: {} {} {} => {}", actualValue, operator, targetValue, result);
			return result;
		} catch (NumberFormatException e) {
			boolean result = switch (operator) {
				case "=" -> actualStr.equalsIgnoreCase(targetStr);
				case "!=" -> !actualStr.equalsIgnoreCase(targetStr);
				default -> false;
			};
			log.debug("String comparison: '{}' {} '{}' => {}", actualStr, operator, targetStr, result);
			return result;
		}
	}
}
