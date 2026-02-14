package com.iviet.ivshs.rule.strategy.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.rule.strategy.DeviceStrategy;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AirConditionDeviceStrategy implements DeviceStrategy {

    private static final String PROP_TEMP = "temp";
    private static final String PROP_MODE = "mode";
    private static final String PROP_FAN_SPEED = "fan_speed";

    @Override
    public boolean supports(DeviceCategory category) {
        return DeviceCategory.AIR_CONDITION.equals(category);
    }

    @Override
    public Object getValue(Long deviceId, String property) {
        if (PROP_TEMP.equalsIgnoreCase(property)) return 24.0;
        if (PROP_MODE.equalsIgnoreCase(property)) return "COOL";
        if (PROP_FAN_SPEED.equalsIgnoreCase(property)) return "HIGH";
        return null;
    }
}
