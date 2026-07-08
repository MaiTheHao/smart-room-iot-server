package com.iviet.ivshs.service.telemetry;

import java.time.Instant;
import java.util.List;

import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public interface SensorTelemetryOrchestratorService {
    List<?> getHistory(Long sensorId, DeviceCategory category, Instant from, Instant to);
    List<?> getHistoryByNaturalId(String naturalId, DeviceCategory category, Instant from, Instant to);
}
