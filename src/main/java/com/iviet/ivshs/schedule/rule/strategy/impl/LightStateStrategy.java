package com.iviet.ivshs.schedule.rule.strategy.impl;

import org.springframework.stereotype.Component;
import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.schedule.rule.strategy.DeviceStateStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LightStateStrategy implements DeviceStateStrategy {

  private final LightDao lightDao;

  private static final String PROP_LEVEL = "level";
  private static final String PROP_POWER = "power";

  @Override
  public boolean supports(DeviceCategory category) {
    return DeviceCategory.LIGHT.equals(category);
  }

  @Override
  public Object fetchState(Long deviceId, String property) {
    if (property == null || deviceId == null) {
      return null;
    }

    Light light = lightDao.findById(deviceId).orElse(null);
    if (light == null) {
      log.warn("Light not found with id: {}", deviceId);
      return null;
    }

    return switch (property.toLowerCase()) {
      case PROP_LEVEL -> light.getLevel();
      case PROP_POWER -> light.getPower();
      default -> {
        log.warn("Property '{}' not supported for LIGHT ID: {}", property, deviceId);
        yield null;
      }
    };
  }
}