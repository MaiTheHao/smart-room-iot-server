package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.service.strategy.DeviceStateStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LightStateStrategy implements DeviceStateStrategy {

  private final LightDao lightDao;

  private static final String PROP_POWER = "power";
  private static final String PROP_LEVEL = "level";

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
      case PROP_POWER -> light.getPower();
      case PROP_LEVEL -> light.getLevel();
      default -> {
        log.warn("Property '{}' not supported for LIGHT ID: {}", property, deviceId);
        yield null;
      }
    };
  }
}
