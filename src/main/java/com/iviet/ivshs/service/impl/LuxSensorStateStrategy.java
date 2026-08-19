package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.dao.LuxSensorDao;
import com.iviet.ivshs.entities.LuxSensor;
import com.iviet.ivshs.service.strategy.SensorStateStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LuxSensorStateStrategy implements SensorStateStrategy {

  private final LuxSensorDao luxSensorDao;

  private static final String PROP_LUX = "lux";

  @Override
  public boolean supports(DeviceCategory category) {
    return DeviceCategory.SENSOR_LUX.equals(category);
  }

  @Override
  public Object fetchState(Long sensorId, String property) {
    if (property == null || sensorId == null) {
      return null;
    }

    LuxSensor lux = luxSensorDao.findById(sensorId).orElse(null);
    if (lux == null) {
      log.warn("Lux sensor not found with id: {}", sensorId);
      return null;
    }

    return switch (property.toLowerCase()) {
      case PROP_LUX -> lux.getCurrentLux();
      default -> {
        log.warn("Property '{}' not supported for LUX sensor ID: {}", property, sensorId);
        yield null;
      }
    };
  }
}
