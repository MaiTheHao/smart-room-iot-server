package com.iviet.ivshs.schedule.rule.strategy.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.entities.Temperature;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.schedule.rule.strategy.SensorStateStrategy;
import com.iviet.ivshs.service.TemperatureService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
  public Object fetchState(Long sensorId, String property) {
    if (sensorId == null || property == null) {
      return null;
    }

    try {
      Temperature sensor = temperatureService.getEntityById(sensorId);
      if (sensor == null) {
        log.warn("Temperature sensor {} not found", sensorId);
        return null;
      }

      return switch (property.toLowerCase()) {
        case PROP_TEMPERATURE -> sensor.getCurrentValue();
        default -> {
          log.warn("Property '{}' not supported for TEMPERATURE sensor in sensor {}", property, sensorId);
          yield null;
        }
      };

    } catch (Exception e) {
      log.error("Failed to fetch TEMPERATURE state for sensor {}: {}", sensorId, e.getMessage(), e);
      return null;
    }
  }
}