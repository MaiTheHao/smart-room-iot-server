package com.iviet.ivshs.rule.strategy;

import com.iviet.ivshs.enumeration.DeviceCategory;

public interface SensorStrategy {

    boolean supports(DeviceCategory category);

    Object getValue(Long sensorId, String property);
}
