package com.iviet.ivshs.startup;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.schedule.telemetry.TelemetryJob;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Startup")
@Component
@Order(30)
@RequiredArgsConstructor
public class TelemetryInitializer implements ApplicationListener<ContextRefreshedEvent> {

  private final SchedulerFactoryBean schedulerFactoryBean;
  private final Environment env;
  private boolean isInitialized = false;

  private int telemetryScanIntervalSeconds;

  @PostConstruct
  private void init() {
    telemetryScanIntervalSeconds = env.getProperty("app.engine.telemetry.scanIntervalSeconds", Integer.class, 300);
    log.info("TelemetryInitializer configured with scan interval: {} seconds", telemetryScanIntervalSeconds);
  }

  @Override
  public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
    if (isInitialized) {
      return;
    }

    try {
      log.info("Module       : [Telemetry Engine] -> [RUNNING]");
      
      long startTime = System.currentTimeMillis();
      scheduleGlobalTelemetryJob();
      long duration = System.currentTimeMillis() - startTime;

      log.info("Module       : [Telemetry Engine] -> [OK]");
      log.info("  - Duration   : {} ms", duration);

      isInitialized = true;

    } catch (Exception e) {
      log.error("Module       : [Telemetry Engine] -> [FAILED]");
      log.error("  - Reason     : {}", e.getMessage());
      log.error("------------------------------------------------------------");
      log.error("Stack trace:", e);
      log.warn("WARNING: Server proceeding without telemetry engine");
      log.warn("ACTION: Check logs and restart server if needed");
    }
  }

  private void scheduleGlobalTelemetryJob() throws SchedulerException {
    Scheduler scheduler = schedulerFactoryBean.getScheduler();

    log.info("Scheduling Telemetry Engine Job with interval: {} seconds", telemetryScanIntervalSeconds);

    JobDetail jobDetail = JobBuilder.newJob(TelemetryJob.class)
        .withIdentity(TelemetryJob.JOB_NAME, TelemetryJob.JOB_GROUP)
        .storeDurably()
        .build();

    if (scheduler.checkExists(jobDetail.getKey())) {
      scheduler.deleteJob(jobDetail.getKey());
    }

    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("TelemetryEngineTrigger", TelemetryJob.JOB_GROUP)
        .startNow()
        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInSeconds(telemetryScanIntervalSeconds)
            .repeatForever())
        .build();

    scheduler.scheduleJob(jobDetail, trigger);
    log.info("Telemetry Engine Job scheduled successfully (Interval: {}s).", telemetryScanIntervalSeconds);
  }
}
