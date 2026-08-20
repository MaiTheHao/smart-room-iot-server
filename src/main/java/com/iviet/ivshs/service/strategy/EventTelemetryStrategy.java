package com.iviet.ivshs.service.strategy;

import com.iviet.ivshs.dto.SensorEventRequestDto;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public interface EventTelemetryStrategy {
    DeviceCategory getSupportedCategory();
    void processData(String naturalId, SensorEventRequestDto request);
}
