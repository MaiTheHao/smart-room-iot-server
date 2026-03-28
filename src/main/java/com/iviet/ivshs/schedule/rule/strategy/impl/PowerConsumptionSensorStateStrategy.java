package com.iviet.ivshs.schedule.rule.strategy.impl;

import org.springframework.stereotype.Component;
import com.iviet.ivshs.entities.PowerConsumption;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.schedule.rule.strategy.SensorStateStrategy;
import com.iviet.ivshs.service.PowerConsumptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PowerConsumptionSensorStateStrategy implements SensorStateStrategy {

  private final PowerConsumptionService powerConsumptionService;

  private static final String PROP_WATT = "watt";

  @Override
  public boolean supports(DeviceCategory category) {
    return DeviceCategory.POWER_CONSUMPTION.equals(category);
  }

  @Override
  public Object fetchState(Long sensorId, String property) {
    if (sensorId == null || property == null) {
      return null;
    }

    try {
      PowerConsumption sensor = powerConsumptionService.getEntityById(sensorId);
      if (sensor == null) {
        log.warn("Power consumption sensor {} not found", sensorId);
        return null;
      }

      return switch (property.toLowerCase()) {
        case PROP_WATT -> sensor.getCurrentWatt();
        default -> {
          log.warn("Property '{}' not supported for POWER_CONSUMPTION sensor in sensor {}", property, sensorId);
          yield null;
        }
      };

    } catch (Exception e) {
      log.error("Failed to fetch POWER_CONSUMPTION state for sensor {}: {}", sensorId, e.getMessage(), e);
      return null;
    }
  }
}