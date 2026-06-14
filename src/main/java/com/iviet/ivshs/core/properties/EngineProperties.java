package com.iviet.ivshs.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Getter
@Component
public class EngineProperties {

  @Value("${app.engine.rule.computeEpsilon}")
  private double ruleComputeEpsilon;

  @Value("${app.engine.rule.scanIntervalSeconds}")
  private int ruleScanIntervalSeconds;

  @Value("${app.engine.rule.telemetryLookbackMinutes}")
  private int ruleTelemetryLookbackMinutes;

  @Value("${app.engine.telemetry.scanIntervalSeconds}")
  private int telemetryScanIntervalSeconds;

  @Value("${app.engine.telemetry.roomStatusLookbackSeconds}")
  private int roomStatusLookbackSeconds;

  @Value("${app.engine.metric_energy.telemetry.intervalSeconds}")
  private int metricEnergyIntervalSeconds;

  @Value("${app.engine.metric_energy.reset.cron}")
  private String metricEnergyResetCron;

  @Value("${app.engine.metric_status.intervalSeconds}")
  private int metricStatusIntervalSeconds;
}
