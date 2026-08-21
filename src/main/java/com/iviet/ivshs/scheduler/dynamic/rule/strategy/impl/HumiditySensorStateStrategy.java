package com.iviet.ivshs.scheduler.dynamic.rule.strategy.impl;

import com.iviet.ivshs.entities.HumiditySensor;
import com.iviet.ivshs.scheduler.dynamic.rule.strategy.SensorStateStrategy;
import com.iviet.ivshs.service.HumidityMetricService;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HumiditySensorStateStrategy implements SensorStateStrategy {

  private final HumidityMetricService humidityMetricService;

  private static final String PROP_HUMIDITY = "humidity";

  @Override
  public boolean supports(DeviceCategory category) {
    return DeviceCategory.HUMIDITY.equals(category);
  }

  @Override
  public Object fetchState(Long sensorId, String property, Instant threshold) {
    if (sensorId == null || property == null) {
      return null;
    }

    try {
      HumiditySensor sensor = humidityMetricService.getSensorById(sensorId);
      if (sensor == null) {
        log.warn("Humidity sensor not found with id: {}", sensorId);
        return null;
      }

      if (!Boolean.TRUE.equals(sensor.getIsActive())) {
        log.warn("Humidity sensor {} is inactive", sensorId);
        return null;
      }

      if (threshold != null
          && (sensor.getUpdatedAt() == null || sensor.getUpdatedAt().isBefore(threshold))) {
        log.warn(
            "Humidity sensor {} data is stale (last updated: {})", sensorId, sensor.getUpdatedAt());
        return null;
      }

      return switch (property.toLowerCase()) {
        case PROP_HUMIDITY -> sensor.getCurrentHumidity();
        default -> {
          log.warn(
              "Property '{}' not supported for HUMIDITY sensor in sensor {}", property, sensorId);
          yield null;
        }
      };
    } catch (Exception e) {
      log.error("Error fetching humidity state for sensorId: {}", sensorId, e);
      return null;
    }
  }
}
