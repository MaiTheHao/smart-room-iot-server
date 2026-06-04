package com.iviet.ivshs.schedule.rule.strategy;

import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public interface SensorStateStrategy {

  boolean supports(DeviceCategory category);

  Object fetchState(Long sensorId, String property);
}
