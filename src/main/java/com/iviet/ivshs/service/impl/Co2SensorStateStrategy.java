package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.dao.Co2SensorDao;
import com.iviet.ivshs.entities.Co2Sensor;
import com.iviet.ivshs.service.strategy.SensorStateStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class Co2SensorStateStrategy implements SensorStateStrategy {

  private final Co2SensorDao co2SensorDao;

  private static final String PROP_CO2 = "co2";

  @Override
  public boolean supports(DeviceCategory category) {
    return DeviceCategory.SENSOR_CO2.equals(category);
  }

  @Override
  public Object fetchState(Long sensorId, String property) {
    if (property == null || sensorId == null) {
      return null;
    }

    Co2Sensor co2 = co2SensorDao.findById(sensorId).orElse(null);
    if (co2 == null) {
      log.warn("CO2 sensor not found with id: {}", sensorId);
      return null;
    }

    return switch (property.toLowerCase()) {
      case PROP_CO2 -> co2.getCurrentCO2();
      default -> {
        log.warn("Property '{}' not supported for CO2 sensor ID: {}", property, sensorId);
        yield null;
      }
    };
  }
}
