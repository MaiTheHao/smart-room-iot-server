package com.iviet.ivshs.schedule.telemetry;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@DisallowConcurrentExecution
public class TelemetryJob implements Job {

  public static final String JOB_NAME = "GLOBAL_TELEMETRY_COLLECTION_JOB";
  public static final String JOB_GROUP = "TELEMETRY_SYSTEM";

  @Autowired
  private TelemetryProcessor telemetryProcessor;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      telemetryProcessor.processAllGateways();
    } catch (Exception e) {
      log.error("Telemetry Collection failed: {}", e.getMessage(), e);
    }
  }
}
