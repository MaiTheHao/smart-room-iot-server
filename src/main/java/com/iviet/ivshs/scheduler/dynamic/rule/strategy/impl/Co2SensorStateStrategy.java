package com.iviet.ivshs.scheduler.dynamic.rule.strategy.impl;

import com.iviet.ivshs.entities.Co2Sensor;
import com.iviet.ivshs.scheduler.dynamic.rule.strategy.SensorStateStrategy;
import com.iviet.ivshs.service.Co2MetricService;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Co2SensorStateStrategy implements SensorStateStrategy {

    private final Co2MetricService co2MetricService;

    @Override
    public boolean supports(DeviceCategory category) {
        return DeviceCategory.SENSOR_CO2.equals(category);
    }

    @Override
    public Object fetchState(Long sensorId, String property) {
        try {
            Co2Sensor sensor = co2MetricService.getSensorById(sensorId);
            if (sensor == null) {
                log.warn("CO2 sensor not found with id: {}", sensorId);
                return null;
            }
            if ("co2".equals(property)) {
                return sensor.getCurrentCO2();
            }
            log.warn("Unsupported property: {} for Co2SensorStateStrategy", property);
            return null;
        } catch (Exception e) {
            log.error("Error fetching CO2 state for sensorId: {}", sensorId, e);
            return null;
        }
    }
}
