package com.iviet.ivshs.schedule.rule.strategy;

import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.shared.enumeration.RuleDataSource;

public interface RuleDataSourceStrategy {

  boolean supports(RuleDataSource dataSource);

  Object fetchValue(RuleCondition condition, Long contextId);
}
