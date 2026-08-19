package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.dto.ConditionValue;
import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.service.registry.SensorStateStrategyRegistry;
import com.iviet.ivshs.service.strategy.ConditionDataSourceStrategy;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorConditionDataSourceStrategy implements ConditionDataSourceStrategy {

  private final SensorStateStrategyRegistry sensorStateStrategyRegistry;

  @Override
  public ConditionDataSource getSupportedDataSource() {
    return ConditionDataSource.SENSOR;
  }

  @Override
  public ConditionValue fetchValue(Condition condition, Long contextId) {
    if (condition == null
        || condition.getSourceTargetId() == null
        || condition.getSourceTargetType() == null
        || condition.getProperty() == null) {
      log.debug(
          "Condition, sourceTargetId, sourceTargetType, or property is null for condition: {}",
          condition != null ? condition.getId() : null);
      return new ConditionValue.MissingValue();
    }

    try {
      DeviceCategory category = condition.getSourceTargetType();
      Long sensorId = Long.parseLong(condition.getSourceTargetId());
      String property = condition.getProperty();

      Object value = sensorStateStrategyRegistry.fetchState(category, sensorId, property);
      log.debug(
          "Fetched state for condition {}: SENSOR [{}] property '{}' = {}",
          condition.getId(),
          sensorId,
          property,
          value);
      return ConditionValue.of(value);
    } catch (NumberFormatException e) {
      log.warn(
          "Invalid sensorId format in condition {}: {}",
          condition.getId(),
          condition.getSourceTargetId());
      return new ConditionValue.MissingValue();
    } catch (Exception e) {
      log.error(
          "Error fetching sensor data for condition {}: {}", condition.getId(), e.getMessage(), e);
      return new ConditionValue.MissingValue();
    }
  }
}
