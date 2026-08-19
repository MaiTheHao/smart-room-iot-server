package com.iviet.ivshs.service.strategy.impl;

import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.service.factory.DeviceStateStrategyFactory;
import com.iviet.ivshs.service.strategy.ConditionDataSourceStrategy;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceConditionDataSourceStrategy implements ConditionDataSourceStrategy {

  private final DeviceStateStrategyFactory deviceStateStrategyFactory;

  @Override
  public ConditionDataSource getSupportedDataSource() {
    return ConditionDataSource.DEVICE;
  }

  @Override
  public Object fetchValue(Condition condition, Long contextId) {
    if (condition == null
        || condition.getSourceTargetId() == null
        || condition.getSourceTargetType() == null
        || condition.getProperty() == null) {
      log.debug(
          "Condition, sourceTargetId, sourceTargetType, or property is null for condition: {}",
          condition != null ? condition.getId() : null);
      return null;
    }

    try {
      DeviceCategory category = condition.getSourceTargetType();
      Long deviceId = Long.parseLong(condition.getSourceTargetId());
      String property = condition.getProperty();

      Object value = deviceStateStrategyFactory.fetchState(category, deviceId, property);
      log.debug(
          "Fetched state for condition {}: DEVICE [{}] property '{}' = {}",
          condition.getId(),
          deviceId,
          property,
          value);
      return value;
    } catch (NumberFormatException e) {
      log.warn(
          "Invalid deviceId format in condition {}: {}",
          condition.getId(),
          condition.getSourceTargetId());
      return null;
    } catch (Exception e) {
      log.error(
          "Error fetching device data for condition {}: {}", condition.getId(), e.getMessage(), e);
      return null;
    }
  }
}
