package com.iviet.ivshs.rule.strategy;

import com.iviet.ivshs.entities.RuleCondition;

public interface RuleDataSourceStrategy {

    /**
     * Provides the current value for the given condition and context.
     *
     * @param condition The rule condition to evaluate.
     * @param contextId The context ID (e.g., roomId or deviceId) depending on logic.
     * @return The current value (Double for sensors, etc.) or null if not found.
     */
    Object provide(RuleCondition condition, Long contextId);

    /**
     * Checks if this provider supports the given condition.
     *
     * @param condition The rule condition.
     * @return true if supported.
     */
    boolean supports(RuleCondition condition);
}
