package com.iviet.ivshs.schedule.rule.strategy.impl;

import org.springframework.stereotype.Component;
import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.schedule.rule.strategy.DeviceStateStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AirConditionStateStrategy implements DeviceStateStrategy {

  private final AirConditionDao airConditionDao;

  private static final String PROP_TEMP = "temp";
  private static final String PROP_MODE = "mode";
  private static final String PROP_FAN_SPEED = "fan_speed";
  private static final String PROP_SWING = "swing";
  private static final String PROP_POWER = "power";

  @Override
  public boolean supports(DeviceCategory category) {
    return DeviceCategory.AIR_CONDITION.equals(category);
  }

  @Override
  public Object fetchState(Long deviceId, String property) {
    if (property == null || deviceId == null) {
      return null;
    }

    AirCondition ac = airConditionDao.findById(deviceId).orElse(null);
    if (ac == null) {
      log.warn("AirCondition not found with id: {}", deviceId);
      return null;
    }

    return switch (property.toLowerCase()) {
      case PROP_POWER -> ac.getPower();
      case PROP_TEMP -> ac.getTemperature();
      case PROP_MODE -> ac.getMode();
      case PROP_FAN_SPEED -> ac.getFanSpeed();
      case PROP_SWING -> ac.getSwing();
      default -> {
        log.warn("Property '{}' not supported for AIR_CONDITION ID: {}", property, deviceId);
        yield null;
      }
    };
  }
}