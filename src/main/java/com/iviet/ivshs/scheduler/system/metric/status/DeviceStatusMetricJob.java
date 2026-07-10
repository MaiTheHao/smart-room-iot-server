package com.iviet.ivshs.scheduler.system.metric.status;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.iviet.ivshs.service.DeviceStatusMetricService;

@Slf4j
@Component
@DisallowConcurrentExecution
public class DeviceStatusMetricJob implements Job {

    public static final String JOB_NAME = "DEVICE_STATUS_METRIC_JOB";
    public static final String JOB_GROUP = "STATUS_METRIC_SYSTEM";

    @Autowired
    private DeviceStatusMetricService deviceStatusMetricService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            deviceStatusMetricService.backupDeviceStatuses();
        } catch (Exception e) {
            log.error("Exec: Device status backup failed: {}", e.getMessage(), e);
        }
    }
}
