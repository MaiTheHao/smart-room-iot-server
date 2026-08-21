package com.iviet.ivshs.scheduler.dynamic.rule.strategy.impl;

import com.iviet.ivshs.entities.PowerConsumption;
import com.iviet.ivshs.scheduler.dynamic.rule.strategy.SensorStateStrategy;
import com.iviet.ivshs.service.PowerConsumptionService;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
  public Object fetchState(Long sensorId, String property, Instant threshold) {
    if (sensorId == null || property == null) {
      return null;
    }

    try {
      PowerConsumption sensor = powerConsumptionService.getEntityById(sensorId);
      if (sensor == null) {
        log.warn("Power consumption sensor {} not found", sensorId);
        return null;
      }

      if (!Boolean.TRUE.equals(sensor.getIsActive())) {
        log.warn("Power consumption sensor {} is inactive", sensorId);
        return null;
      }

      if (threshold != null
          && (sensor.getUpdatedAt() == null || sensor.getUpdatedAt().isBefore(threshold))) {
        log.warn(
            "Power consumption sensor {} data is stale (last updated: {})",
            sensorId,
            sensor.getUpdatedAt());
        return null;
      }

      return switch (property.toLowerCase()) {
        case PROP_WATT -> sensor.getCurrentWatt();
        default -> {
          log.warn(
              "Property '{}' not supported for POWER_CONSUMPTION sensor in sensor {}",
              property,
              sensorId);
          yield null;
        }
      };

    } catch (Exception e) {
      log.error(
          "Failed to fetch POWER_CONSUMPTION state for sensor {}: {}", sensorId, e.getMessage(), e);
      return null;
    }
  }
}
