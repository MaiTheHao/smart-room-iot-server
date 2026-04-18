package com.iviet.ivshs.schedule.metric.energy;

import com.iviet.ivshs.service.EnergyMetricService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j(topic = "ENERGY-RESET-JOB")
@Component
@DisallowConcurrentExecution
public class EnergyMetricResetJob implements Job {

    public static final String JOB_NAME  = "ENERGY_METRIC_RESET_JOB";
    public static final String JOB_GROUP = "ENERGY_METRIC_SYSTEM";

    @Autowired
    private EnergyMetricService energyMetricService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            energyMetricService.resetGateways();
        } catch (Exception e) {
            log.error("Exec: Reset job failed: {}", e.getMessage(), e);
        }
    }
}
