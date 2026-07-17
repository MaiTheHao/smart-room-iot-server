package com.iviet.ivshs.service;

import com.iviet.ivshs.service.strategy.MetricServiceStrategy;
import com.iviet.ivshs.service.strategy.SensorMetadataServiceStrategy;
import com.iviet.ivshs.service.strategy.SensorTelemetryServiceStrategy;
import com.iviet.ivshs.service.strategy.TelemetryCRUDServiceStrategy;
import com.iviet.ivshs.entities.Co2Sensor;

/**
 * Service interface for handling CO2 Metrics, extending the platform's strategies.
 */
public interface Co2MetricService extends TelemetryCRUDServiceStrategy, MetricServiceStrategy, SensorTelemetryServiceStrategy, SensorMetadataServiceStrategy<Co2Sensor> {
}
