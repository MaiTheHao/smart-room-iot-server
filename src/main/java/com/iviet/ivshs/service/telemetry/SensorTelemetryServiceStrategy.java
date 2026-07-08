package com.iviet.ivshs.service.telemetry;

import java.time.Instant;
import java.util.List;

import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public interface SensorTelemetryServiceStrategy {
    DeviceCategory getSupportedCategory();
    List<?> getHistory(Long sensorId, Instant from, Instant to);
    List<?> getHistoryByNaturalId(String naturalId, Instant from, Instant to);
}
