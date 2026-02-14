package com.iviet.ivshs.rule.strategy;

import com.iviet.ivshs.enumeration.DeviceCategory;

public interface DeviceStrategy {

    boolean supports(DeviceCategory category);

    Object getValue(Long deviceId, String property);
}
