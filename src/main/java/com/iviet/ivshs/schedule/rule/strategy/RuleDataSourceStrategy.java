package com.iviet.ivshs.schedule.rule.strategy;

import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.enumeration.RuleDataSource;

public interface RuleDataSourceStrategy {

  /**
   * Kiểm tra xem strategy này có hỗ trợ DataSource tương ứng không
   */
  boolean supports(RuleDataSource dataSource);

  /**
   * Lấy giá trị hiện tại dựa trên condition
   */
  Object fetchValue(RuleCondition condition, Long contextId);
}

