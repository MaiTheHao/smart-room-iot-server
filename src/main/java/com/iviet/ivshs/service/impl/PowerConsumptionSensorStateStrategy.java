package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.dao.PowerConsumptionDao;
import com.iviet.ivshs.entities.PowerConsumption;
import com.iviet.ivshs.service.strategy.SensorStateStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PowerConsumptionSensorStateStrategy implements SensorStateStrategy {

  private final PowerConsumptionDao powerConsumptionDao;

  private static final String PROP_WATT = "watt";

  @Override
  public boolean supports(DeviceCategory category) {
    return DeviceCategory.POWER_CONSUMPTION.equals(category);
  }

  @Override
  public Object fetchState(Long sensorId, String property) {
    if (property == null || sensorId == null) {
      return null;
    }

    PowerConsumption pc = powerConsumptionDao.findById(sensorId).orElse(null);
    if (pc == null) {
      log.warn("PowerConsumption sensor not found with id: {}", sensorId);
      return null;
    }

    return switch (property.toLowerCase()) {
      case PROP_WATT -> pc.getCurrentWatt();
      default -> {
        log.warn(
            "Property '{}' not supported for POWER_CONSUMPTION sensor ID: {}", property, sensorId);
        yield null;
      }
    };
  }
}
