package com.iviet.ivshs.schedule.rule.strategy.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.enumeration.RuleDataSource;
import com.iviet.ivshs.schedule.rule.strategy.RuleDataSourceStrategy;
import com.iviet.ivshs.schedule.rule.strategy.SensorStateStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorDataSourceStrategy implements RuleDataSourceStrategy {

  private final List<SensorStateStrategy> sensorStrategies;

  @Override
  public boolean supports(RuleDataSource dataSource) {
    return RuleDataSource.SENSOR.equals(dataSource);
  }

  @Override
  public Object fetchValue(RuleCondition condition, Long contextId) {
    try {
      JsonNode params = condition.getResourceParam();
      if (params == null) {
        log.debug("Resource params are null for condition: {}", condition.getId());
        return null;
      }

      String categoryStr = params.path("category").asText();
      DeviceCategory category = DeviceCategory.valueOf(categoryStr);
      Long sensorId = params.path("sensorId").asLong();
      String property = params.path("property").asText();

      for (SensorStateStrategy strategy : sensorStrategies) {
        if (strategy.supports(category)) {
          Object value = strategy.fetchState(sensorId, property);
          log.debug("Fetched state for condition {}: SENSOR [{}] property '{}' = {}", 
                    condition.getId(), sensorId, property, value);
          return value;
        }
      }
      log.warn("No sensor strategy found for category '{}' in condition {}", categoryStr, condition.getId());
      return null;
    } catch (IllegalArgumentException e) {
      log.warn("Invalid category in condition {}: {}", condition.getId(), e.getMessage());
      return null;
    } catch (Exception e) {
      log.error("Error fetching sensor data for condition {}: {}", condition.getId(), e.getMessage(), e);
      return null;
    }
  }
}