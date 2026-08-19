package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.service.strategy.DeviceStateStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

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

    return switch (property.toLowerCase()) {
      case PROP_POWER -> fan.getPower();
      case PROP_SPEED -> fan.getSpeed();
      case PROP_MODE -> fan.getMode();
      case PROP_SWING -> fan.getSwing();
      case PROP_LIGHT -> fan.getLight();
      default -> {
        log.warn("Property '{}' not supported for FAN ID: {}", property, deviceId);
        yield null;
      }
    };
  }
}
