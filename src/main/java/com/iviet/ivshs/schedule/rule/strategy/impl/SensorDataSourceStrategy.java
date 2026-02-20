package com.iviet.ivshs.schedule.rule.strategy.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final ObjectMapper objectMapper;

  @Override
  public boolean supports(RuleDataSource dataSource) {
    return RuleDataSource.SENSOR.equals(dataSource);
  }

  @Override
  public Object fetchValue(RuleCondition condition, Long contextId) {
    try {
      JsonNode params = objectMapper.readTree(condition.getResourceParam());
      if (params == null) {
        return null;
      }

      String categoryStr = params.path("category").asText();
      DeviceCategory category = DeviceCategory.valueOf(categoryStr);
      Long sensorId = params.path("sensorId").asLong();
      String property = params.path("property").asText();

      for (SensorStateStrategy strategy : sensorStrategies) {
        if (strategy.supports(category)) {
          return strategy.fetchState(sensorId, property);
        }
      }
      log.warn("No sensor strategy found for category: {}", categoryStr);
      return null;
    } catch (Exception e) {
      log.error("Error in SensorDataProvider: {}", e.getMessage());
      return null;
    }
  }
}
