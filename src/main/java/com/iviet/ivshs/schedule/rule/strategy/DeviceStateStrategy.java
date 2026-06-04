package com.iviet.ivshs.schedule.rule.strategy;

import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public interface DeviceStateStrategy {

  boolean supports(DeviceCategory category);

  Object fetchState(Long deviceId, String property);
}
