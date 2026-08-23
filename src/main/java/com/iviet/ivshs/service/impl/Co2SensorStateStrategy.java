package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.entities.Co2Sensor;
import com.iviet.ivshs.service.Co2MetricService;
import com.iviet.ivshs.service.strategy.SensorStateStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Co2SensorStateStrategy implements SensorStateStrategy {

  private final Co2MetricService co2MetricService;

  private static final String PROP_CO2 = "co2";

  @Override
  public boolean supports(DeviceCategory category) {
    return DeviceCategory.SENSOR_CO2.equals(category);
  }

  @Override
  public Object fetchState(Long sensorId, String property, Instant threshold) {
    if (sensorId == null || property == null) {
      return null;
    }

    try {
      Co2Sensor sensor = co2MetricService.getSensorById(sensorId);
      if (sensor == null) {
        log.warn("CO2 sensor not found with id: {}", sensorId);
        return null;
      }

      if (!Boolean.TRUE.equals(sensor.getIsActive())) {
        log.warn("CO2 sensor {} is inactive", sensorId);
        return null;
      }

      if (threshold != null
          && (sensor.getUpdatedAt() == null || sensor.getUpdatedAt().isBefore(threshold))) {
        log.warn("CO2 sensor {} data is stale (last updated: {})", sensorId, sensor.getUpdatedAt());
        return null;
      }

      return switch (property.toLowerCase()) {
        case PROP_CO2 -> sensor.getCurrentCO2();
        default -> {
          log.warn("Property '{}' not supported for CO2 sensor in sensor {}", property, sensorId);
          yield null;
        }
      };
    } catch (Exception e) {
      log.error("Error fetching CO2 state for sensorId: {}", sensorId, e);
      return null;
    }
  }
}
