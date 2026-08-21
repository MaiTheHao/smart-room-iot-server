package com.iviet.ivshs.scheduler.dynamic.rule.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.core.properties.EngineProperties;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.scheduler.dynamic.rule.strategy.RuleDataSourceStrategy;
import com.iviet.ivshs.scheduler.dynamic.rule.strategy.SensorStateStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.RuleDataSource;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorDataSourceStrategy implements RuleDataSourceStrategy {

  private final List<SensorStateStrategy> sensorStrategies;
  private final EngineProperties engineProperties;

  private int freshnessMinutes;

  @PostConstruct
  public void init() {
    freshnessMinutes = engineProperties.getRuleSensorFreshnessMinutes();
  }

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

      String categoryStr = params.path("category").asText(null);
      if (categoryStr == null || categoryStr.isBlank()) {
        log.warn("Category is missing in SENSOR resourceParam for condition {}", condition.getId());
        return null;
      }

      Long sensorId = params.path("sensorId").asLong(0L);
      if (sensorId == 0L) {
        log.warn(
            "Sensor ID is missing or 0 in SENSOR resourceParam for condition {}",
            condition.getId());
        return null;
      }

      String property = params.path("property").asText(null);
      if (property == null || property.isBlank()) {
        log.warn("Property is missing in SENSOR resourceParam for condition {}", condition.getId());
        return null;
      }

      DeviceCategory category = DeviceCategory.valueOf(categoryStr);
      Instant now = Instant.now();
      Instant thresholdTime = now.minus(freshnessMinutes, ChronoUnit.MINUTES);

      for (SensorStateStrategy strategy : sensorStrategies) {
        if (strategy.supports(category)) {
          Object value = strategy.fetchState(sensorId, property, thresholdTime);
          log.debug(
              "Fetched state for condition {}: SENSOR [{}] property '{}' = {}",
              condition.getId(),
              sensorId,
              property,
              value);
          return value;
        }
      }
      log.warn(
          "No sensor strategy found for category '{}' in condition {}",
          categoryStr,
          condition.getId());
      return null;
    } catch (IllegalArgumentException e) {
      log.warn("Invalid category in condition {}: {}", condition.getId(), e.getMessage());
      return null;
    } catch (Exception e) {
      log.error(
          "Error fetching sensor data for condition {}: {}", condition.getId(), e.getMessage(), e);
      return null;
    }
  }
}
