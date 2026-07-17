package com.iviet.ivshs.service;

import com.iviet.ivshs.service.strategy.MetricServiceStrategy;
import com.iviet.ivshs.service.strategy.SensorMetadataServiceStrategy;
import com.iviet.ivshs.service.strategy.SensorTelemetryServiceStrategy;
import com.iviet.ivshs.entities.HumiditySensor;
import com.iviet.ivshs.service.strategy.TelemetryCRUDServiceStrategy;

/**
 * Service interface for handling Humidity Metrics, extending the platform's strategies.
 */
public interface HumidityMetricService extends TelemetryCRUDServiceStrategy, MetricServiceStrategy, SensorTelemetryServiceStrategy, SensorMetadataServiceStrategy<HumiditySensor> {
}
