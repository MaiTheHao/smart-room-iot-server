package com.iviet.ivshs.service;

import com.iviet.ivshs.service.strategy.MetricServiceStrategy;
import com.iviet.ivshs.service.strategy.SensorTelemetryServiceStrategy;
import com.iviet.ivshs.service.strategy.TelemetryCRUDServiceStrategy;

/**
 * Service interface for handling CO2 Metrics, extending the platform's strategies.
 */
public interface Co2MetricService extends TelemetryCRUDServiceStrategy, MetricServiceStrategy, SensorTelemetryServiceStrategy {
}
