package com.iviet.ivshs.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Getter
@Component
public class AppProperties {

    @Value("${app.engine.rule.computeEpsilon:0.05}")
    private double ruleComputeEpsilon;

    @Value("${app.engine.rule.scanIntervalSeconds:300}")
    private int ruleScanIntervalSeconds;

    @Value("${app.engine.rule.telemetryLookbackMinutes:600}")
    private int ruleTelemetryLookbackMinutes;

    @Value("${app.engine.telemetry.scanIntervalSeconds:120}")
    private int telemetryScanIntervalSeconds;

    @Value("${app.engine.telemetry.roomStatusLookbackSeconds:600}")
    private int roomStatusLookbackSeconds;

    @Value("${app.engine.metric_energy.telemetry.intervalSeconds:300}")
    private int metricEnergyIntervalSeconds;

    @Value("${app.engine.metric_energy.reset.cron:0 0 0 * * ?}")
    private String metricEnergyResetCron;

    @Value("${app.engine.metric_status.intervalSeconds:300}")
    private int metricStatusIntervalSeconds;
}
