package com.iviet.ivshs.rule.strategy.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.rule.strategy.SensorStrategy;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TemperatureSensorStrategy implements SensorStrategy {

    @Override
    public boolean supports(DeviceCategory category) {
        return DeviceCategory.TEMPERATURE.equals(category); 
    }

    @Override
    public Object getValue(Long sensorId, String property) {
        return 28.5;
    }
}
