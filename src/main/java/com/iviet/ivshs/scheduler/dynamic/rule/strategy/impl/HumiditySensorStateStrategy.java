package com.iviet.ivshs.scheduler.dynamic.rule.strategy.impl;

import com.iviet.ivshs.entities.HumiditySensor;
import com.iviet.ivshs.scheduler.dynamic.rule.strategy.SensorStateStrategy;
import com.iviet.ivshs.service.HumidityMetricService;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HumiditySensorStateStrategy implements SensorStateStrategy {

    private final HumidityMetricService humidityMetricService;

    @Override
    public boolean supports(DeviceCategory category) {
        return DeviceCategory.HUMIDITY.equals(category);
    }

    @Override
    public Object fetchState(Long sensorId, String property) {
        try {
            HumiditySensor sensor = humidityMetricService.getSensorById(sensorId);
            if (sensor == null) {
                log.warn("Humidity sensor not found with id: {}", sensorId);
                return null;
            }
            if ("humidity".equals(property)) {
                return sensor.getCurrentHumidity();
            }
            log.warn("Unsupported property: {} for HumiditySensorStateStrategy", property);
            return null;
        } catch (Exception e) {
            log.error("Error fetching humidity state for sensorId: {}", sensorId, e);
            return null;
        }
    }
}
