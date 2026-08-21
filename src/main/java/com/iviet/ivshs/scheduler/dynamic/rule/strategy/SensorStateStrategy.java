package com.iviet.ivshs.scheduler.dynamic.rule.strategy;

import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.time.Instant;

public interface SensorStateStrategy {

  boolean supports(DeviceCategory category);

  default Object fetchState(Long sensorId, String property) {
    return fetchState(sensorId, property, null);
  }

  Object fetchState(Long sensorId, String property, Instant threshold);
}
