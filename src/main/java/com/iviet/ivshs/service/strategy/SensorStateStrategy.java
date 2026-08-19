package com.iviet.ivshs.service.strategy;

import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public interface SensorStateStrategy {

  boolean supports(DeviceCategory category);

  Object fetchState(Long sensorId, String property);
}
