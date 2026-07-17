package com.iviet.ivshs.scheduler.dynamic.rule.strategy.impl;

import com.iviet.ivshs.entities.LuxSensor;
import com.iviet.ivshs.scheduler.dynamic.rule.strategy.SensorStateStrategy;
import com.iviet.ivshs.service.LuxMetricService;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LuxSensorStateStrategy implements SensorStateStrategy {

    private final LuxMetricService luxMetricService;

    @Override
    public boolean supports(DeviceCategory category) {
        return DeviceCategory.SENSOR_LUX.equals(category);
    }

    @Override
    public Object fetchState(Long sensorId, String property) {
        try {
            LuxSensor sensor = luxMetricService.getSensorById(sensorId);
            if (sensor == null) {
                log.warn("Lux sensor not found with id: {}", sensorId);
                return null;
            }
            if ("lux".equals(property)) {
                return sensor.getCurrentLux();
            }
            log.warn("Unsupported property: {} for LuxSensorStateStrategy", property);
            return null;
        } catch (Exception e) {
            log.error("Error fetching lux state for sensorId: {}", sensorId, e);
            return null;
        }
    }
}
