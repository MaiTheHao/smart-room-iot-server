package com.iviet.ivshs.schedule.rule.strategy;

import com.iviet.ivshs.enumeration.DeviceCategory;

public interface RuleActuatorStrategy {

    boolean supports(DeviceCategory category);

    Object getValue(Long deviceId, String property);
}
