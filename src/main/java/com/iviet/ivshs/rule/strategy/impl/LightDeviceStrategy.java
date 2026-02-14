package com.iviet.ivshs.rule.strategy.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.rule.strategy.DeviceStrategy;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LightDeviceStrategy implements DeviceStrategy {

    private static final String PROP_LEVEL = "level";
    private static final String PROP_STATUS = "status";

    @Override
    public boolean supports(DeviceCategory category) {
        return DeviceCategory.LIGHT.equals(category);
    }

    @Override
    public Object getValue(Long deviceId, String property) {
        if (PROP_LEVEL.equalsIgnoreCase(property)) return 80.0;
        if (PROP_STATUS.equalsIgnoreCase(property)) return "ON";
        return null;
    }
}
