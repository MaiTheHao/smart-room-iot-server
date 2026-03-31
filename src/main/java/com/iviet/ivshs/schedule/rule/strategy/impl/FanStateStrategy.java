package com.iviet.ivshs.schedule.rule.strategy.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.FanIr;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.schedule.rule.strategy.DeviceStateStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FanStateStrategy implements DeviceStateStrategy {

  private final FanDao fanDao;

  private static final String PROP_POWER = "power";
  private static final String PROP_SPEED = "speed";
  private static final String PROP_MODE = "mode";
  private static final String PROP_SWING = "swing";
  private static final String PROP_LIGHT = "light";

  @Override
  public boolean supports(DeviceCategory category) {
    return DeviceCategory.FAN.equals(category);
  }

  @Override
  public Object fetchState(Long deviceId, String property) {
    if (property == null || deviceId == null) {
      return null;
    }

    Fan fan = fanDao.findById(deviceId).orElse(null);
    if (fan == null) {
      log.warn("Fan not found with id: {}", deviceId);
      return null;
    }

    String lowerProp = property.toLowerCase();
    
    if (PROP_POWER.equals(lowerProp)) return fan.getPower();
    if (PROP_SPEED.equals(lowerProp)) return fan.getSpeed();
    if (fan instanceof FanIr fanIr) {
      return switch (lowerProp) {
        case PROP_MODE -> fanIr.getMode();
        case PROP_SWING -> fanIr.getSwing();
        case PROP_LIGHT -> fanIr.getLight();
        default -> {
          log.warn("Property '{}' not supported for FAN IR ID: {}", property, deviceId);
          yield null;
        }
      };
    }

    log.warn("Property '{}' not supported for FAN ID: {} (Type: {})", property, deviceId, fan.getClass().getSimpleName());
    return null;
  }
}
