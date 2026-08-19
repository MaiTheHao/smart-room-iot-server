package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.dao.HumiditySensorDao;
import com.iviet.ivshs.entities.HumiditySensor;
import com.iviet.ivshs.service.strategy.SensorStateStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HumiditySensorStateStrategy implements SensorStateStrategy {

  private final HumiditySensorDao humiditySensorDao;

  private static final String PROP_HUMIDITY = "humidity";

  @Override
  public boolean supports(DeviceCategory category) {
    return DeviceCategory.HUMIDITY.equals(category);
  }

  @Override
  public Object fetchState(Long sensorId, String property) {
    if (property == null || sensorId == null) {
      return null;
    }

    HumiditySensor humidity = humiditySensorDao.findById(sensorId).orElse(null);
    if (humidity == null) {
      log.warn("Humidity sensor not found with id: {}", sensorId);
      return null;
    }

    return switch (property.toLowerCase()) {
      case PROP_HUMIDITY -> humidity.getCurrentHumidity();
      default -> {
        log.warn("Property '{}' not supported for HUMIDITY sensor ID: {}", property, sensorId);
        yield null;
      }
    };
  }
}
