package com.iviet.ivshs.schedule.metric.status;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j(topic = "STATUS-JOB")
@Component
@DisallowConcurrentExecution
public class DeviceStatusMetricJob implements Job {

    public static final String JOB_NAME  = "DEVICE_STATUS_METRIC_JOB";
    public static final String JOB_GROUP = "STATUS_METRIC_SYSTEM";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("Exec: Starting device status backup...");
            // TODO: Implementation logic to fetch all devices and backup status
            // This is a special job to get all devices in the system to backup status
            log.info("Exec: Device status backup completed successfully");
        } catch (Exception e) {
            log.error("Exec: Device status backup failed: {}", e.getMessage(), e);
        }
    }
}
