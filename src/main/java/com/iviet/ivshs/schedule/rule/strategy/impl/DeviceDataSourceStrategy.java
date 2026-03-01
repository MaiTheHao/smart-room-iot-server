package com.iviet.ivshs.schedule.rule.strategy.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.enumeration.RuleDataSource;
import com.iviet.ivshs.schedule.rule.strategy.DeviceStateStrategy;
import com.iviet.ivshs.schedule.rule.strategy.RuleDataSourceStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceDataSourceStrategy implements RuleDataSourceStrategy {

  private final List<DeviceStateStrategy> deviceStrategies;
  @Override
  public boolean supports(RuleDataSource dataSource) {
    return RuleDataSource.DEVICE.equals(dataSource);
  }

  @Override
  public Object fetchValue(RuleCondition condition, Long contextId) {
    try {
      JsonNode params = condition.getResourceParam();
      if (params == null) {
        return null;
      }

      String categoryStr = params.path("category").asText();
      DeviceCategory category = DeviceCategory.valueOf(categoryStr);
      Long deviceId = params.path("deviceId").asLong();
      String property = params.path("property").asText();

      // Loop qua các loại device (AirCondition, Light,...)
      for (DeviceStateStrategy strategy : deviceStrategies) {
        if (strategy.supports(category)) {
          return strategy.fetchState(deviceId, property);
        }
      }
      log.warn("No device strategy found for category: {}", categoryStr);
      return null;
    } catch (Exception e) {
      log.error("Error in DeviceDataProvider: {}", e.getMessage());
      return null;
    }
  }
}
