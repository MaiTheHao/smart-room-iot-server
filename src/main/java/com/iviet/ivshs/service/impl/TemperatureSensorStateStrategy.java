package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.dao.TemperatureDao;
import com.iviet.ivshs.entities.Temperature;
import com.iviet.ivshs.service.strategy.SensorStateStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemperatureSensorStateStrategy implements SensorStateStrategy {

  private final TemperatureDao temperatureDao;

  private static final String PROP_TEMPERATURE = "temperature";

  @Override
  public boolean supports(DeviceCategory category) {
    return DeviceCategory.TEMPERATURE.equals(category);
  }

  @Override
  public Object fetchState(Long sensorId, String property) {
    if (property == null || sensorId == null) {
      return null;
    }

    Temperature temp = temperatureDao.findById(sensorId).orElse(null);
    if (temp == null) {
      log.warn("Temperature sensor not found with id: {}", sensorId);
      return null;
    }

    return switch (property.toLowerCase()) {
      case PROP_TEMPERATURE -> temp.getCurrentValue();
      default -> {
        log.warn("Property '{}' not supported for TEMPERATURE sensor ID: {}", property, sensorId);
        yield null;
      }
    };
  }
}
