package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.entities.LuxSensor;
import com.iviet.ivshs.service.LuxMetricService;
import com.iviet.ivshs.service.strategy.SensorStateStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LuxSensorStateStrategy implements SensorStateStrategy {

  private final LuxMetricService luxMetricService;

  private static final String PROP_LUX = "lux";

  @Override
  public boolean supports(DeviceCategory category) {
    return DeviceCategory.SENSOR_LUX.equals(category);
  }

  @Override
  public Object fetchState(Long sensorId, String property, Instant threshold) {
    if (sensorId == null || property == null) {
      return null;
    }

    try {
      LuxSensor sensor = luxMetricService.getSensorById(sensorId);
      if (sensor == null) {
        log.warn("Lux sensor not found with id: {}", sensorId);
        return null;
      }

      if (!Boolean.TRUE.equals(sensor.getIsActive())) {
        log.warn("Lux sensor {} is inactive", sensorId);
        return null;
      }

      if (threshold != null
          && (sensor.getUpdatedAt() == null || sensor.getUpdatedAt().isBefore(threshold))) {
        log.warn("Lux sensor {} data is stale (last updated: {})", sensorId, sensor.getUpdatedAt());
        return null;
      }

      return switch (property.toLowerCase()) {
        case PROP_LUX -> sensor.getCurrentLux();
        default -> {
          log.warn("Property '{}' not supported for LUX sensor in sensor {}", property, sensorId);
          yield null;
        }
      };
    } catch (Exception e) {
      log.error("Error fetching lux state for sensorId: {}", sensorId, e);
      return null;
    }
  }
}
