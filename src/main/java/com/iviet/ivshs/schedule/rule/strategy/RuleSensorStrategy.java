package com.iviet.ivshs.schedule.rule.strategy;

import com.iviet.ivshs.enumeration.DeviceCategory;

public interface RuleSensorStrategy {

    boolean supports(DeviceCategory category);

    Object getValue(Long sensorId, String property);
}
