package com.iviet.ivshs.service.strategy;

import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public interface DeviceStateStrategy {

  boolean supports(DeviceCategory category);

  Object fetchState(Long deviceId, String property);
}
