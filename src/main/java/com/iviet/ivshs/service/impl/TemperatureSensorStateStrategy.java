package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.entities.Temperature;
import com.iviet.ivshs.service.TemperatureService;
import com.iviet.ivshs.service.strategy.SensorStateStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemperatureSensorStateStrategy implements SensorStateStrategy {

  private final TemperatureService temperatureService;

  private static final String PROP_TEMPERATURE = "temperature";

  @Override
  public boolean supports(DeviceCategory category) {
    return DeviceCategory.TEMPERATURE.equals(category);
  }

  @Override
  public Object fetchState(Long sensorId, String property, Instant threshold) {
    if (sensorId == null || property == null) {
      return null;
    }

    try {
      Temperature sensor = temperatureService.getEntityById(sensorId);
      if (sensor == null) {
        log.warn("Temperature sensor {} not found", sensorId);
        return null;
      }

      if (!Boolean.TRUE.equals(sensor.getIsActive())) {
        log.warn("Temperature sensor {} is inactive", sensorId);
        return null;
      }

      if (threshold != null
          && (sensor.getUpdatedAt() == null || sensor.getUpdatedAt().isBefore(threshold))) {
        log.warn(
            "Temperature sensor {} data is stale (last updated: {})",
            sensorId,
            sensor.getUpdatedAt());
        return null;
      }

      return switch (property.toLowerCase()) {
        case PROP_TEMPERATURE -> sensor.getCurrentValue();
        default -> {
          log.warn(
              "Property '{}' not supported for TEMPERATURE sensor in sensor {}",
              property,
              sensorId);
          yield null;
        }
      };

    } catch (Exception e) {
      log.error("Failed to fetch TEMPERATURE state for sensor {}: {}", sensorId, e.getMessage(), e);
      return null;
    }
  }
}
