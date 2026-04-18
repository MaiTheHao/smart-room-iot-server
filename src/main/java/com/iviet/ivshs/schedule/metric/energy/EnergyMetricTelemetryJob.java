package com.iviet.ivshs.schedule.metric.energy;

import com.iviet.ivshs.service.EnergyMetricService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j(topic = "ENERGY-JOB")
@Component
@DisallowConcurrentExecution
public class EnergyMetricTelemetryJob implements Job {

    public static final String JOB_NAME  = "ENERGY_METRIC_TELEMETRY_JOB";
    public static final String JOB_GROUP = "ENERGY_METRIC_SYSTEM";

    @Autowired
    private EnergyMetricService energyMetricService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            energyMetricService.fetchFromGateways();
        } catch (Exception e) {
            log.error("Exec: Telemetry collection failed: {}", e.getMessage(), e);
        }
    }
}
