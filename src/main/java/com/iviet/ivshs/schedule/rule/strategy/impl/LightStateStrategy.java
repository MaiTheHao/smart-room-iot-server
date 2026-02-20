package com.iviet.ivshs.schedule.rule.strategy.impl;

import org.springframework.stereotype.Component;
import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.schedule.rule.strategy.DeviceStateStrategy;

import lombok.RequiredArgsConstructor;

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
    if (property == null)
      return null;

    Light light = getLight(deviceId);

    return switch (property.toLowerCase()) {
      case PROP_LEVEL -> light.getLevel();
      case PROP_POWER -> light.getPower();
      default -> null;
    };
  }

  private Light getLight(Long deviceId) {
    return lightDao.findById(deviceId)
        .orElseThrow(() -> new RuntimeException("Light not found with id: " + deviceId));
  }
}
