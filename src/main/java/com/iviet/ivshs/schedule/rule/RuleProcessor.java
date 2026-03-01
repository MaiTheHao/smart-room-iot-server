package com.iviet.ivshs.schedule.rule;

import com.iviet.ivshs.entities.Rule;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.enumeration.ConditionLogic;
import com.iviet.ivshs.enumeration.ConditionOperator;
import com.iviet.ivshs.schedule.rule.strategy.RuleDataSourceStrategy;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleProcessor {

	private final Environment env;
	private final List<RuleDataSourceStrategy> ruleDataSourceStrategies;
	private static final double DEFAULT_EPSILON = 0.05;
	private double EPSILON = DEFAULT_EPSILON;

	@PostConstruct 
	private void init() {
		Double epsilonConfig = env.getProperty("app.engine.rule.computeEpsilon", Double.class);
		if (epsilonConfig != null) EPSILON = epsilonConfig;
		log.info("RuleProcessor initialized with epsilon = {}", EPSILON);
	}

	public boolean matches(Rule rule) {
		List<RuleCondition> conditions = rule.getConditions();
		
		if (conditions == null || conditions.isEmpty()) {
			log.warn("Rule {} has no conditions", rule.getId());
			return false;
		}

		log.info("Evaluating rule {} with {} conditions", rule.getId(), conditions.size());

		return conditions.stream()
				.sorted(Comparator.comparingInt(RuleCondition::getSortOrder))
				.reduce(new EvaluationContext(), this::accumulateResult, (a, b) -> a)
				.isFinalResult();
	}

	private EvaluationContext accumulateResult(EvaluationContext ctx, RuleCondition cond) {
		boolean isMet = evaluateCondition(cond, ctx.getRoomId());
		
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
		
		ctx.setPrevLogic(cond.getNextLogic());
		return ctx;
	}

	private boolean evaluateCondition(RuleCondition cond, Long roomId) {
		return ruleDataSourceStrategies.stream()
				.filter(s -> s.supports(cond.getDataSource()))
				.findFirst()
				.map(strategy -> {
					try {
						Object val = strategy.fetchValue(cond, roomId);
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

	@lombok.Data
	private static class EvaluationContext {
		private boolean finalResult = true;
		private boolean isFirst = true;
		private ConditionLogic prevLogic;
		private Long roomId;
	}
}
