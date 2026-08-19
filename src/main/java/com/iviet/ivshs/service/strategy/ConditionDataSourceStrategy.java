package com.iviet.ivshs.service.strategy;

import com.iviet.ivshs.dto.ConditionValue;
import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;

public interface ConditionDataSourceStrategy {

  ConditionDataSource getSupportedDataSource();

  ConditionValue fetchValue(Condition condition, Long contextId);
}
