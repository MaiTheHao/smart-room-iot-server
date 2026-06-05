package com.iviet.ivshs.core.startup;

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
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.core.properties.EngineProperties;
import com.iviet.ivshs.scheduler.telemetry.TelemetryJob;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(30)
@RequiredArgsConstructor
public class TelemetryInitializer implements ApplicationListener<ContextRefreshedEvent> {

  private final SchedulerFactoryBean schedulerFactoryBean;
  private final EngineProperties engineProperties;
  private boolean isInitialized = false;

  private int telemetryScanIntervalSeconds;

  @PostConstruct
  private void init() {
    telemetryScanIntervalSeconds = engineProperties.getTelemetryScanIntervalSeconds();
    log.info("Telemetry engine config: scanIntervalSeconds={}", telemetryScanIntervalSeconds);
  }

  @Override
  public void onApplicationEvent(@NonNull
  ContextRefreshedEvent event) {
    if (isInitialized || telemetryScanIntervalSeconds <= 0) {
      return;
    }

    try {
      log.info("Starting Telemetry Engine initialization");

      long startTime = System.currentTimeMillis();
      scheduleGlobalTelemetryJob();
      long duration = System.currentTimeMillis() - startTime;

      log.info("Telemetry Engine initialized successfully: duration={}ms", duration);

      isInitialized = true;

    } catch (Exception e) {
      log.error("Telemetry Engine initialization failed", e);
      log.warn("Server proceeding without Telemetry Engine. Please check database/Quartz scheduler status.");
    }
  }

  private void scheduleGlobalTelemetryJob() throws SchedulerException {
    Scheduler scheduler = schedulerFactoryBean.getScheduler();

    JobDetail jobDetail = JobBuilder.newJob(TelemetryJob.class).withIdentity(TelemetryJob.JOB_NAME, TelemetryJob.JOB_GROUP).storeDurably().build();

    if (scheduler.checkExists(jobDetail.getKey())) {
      scheduler.deleteJob(jobDetail.getKey());
    }

    Trigger trigger =
        TriggerBuilder.newTrigger().withIdentity("TelemetryEngineTrigger", TelemetryJob.JOB_GROUP).startNow().withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(telemetryScanIntervalSeconds).repeatForever()).build();

    scheduler.scheduleJob(jobDetail, trigger);
    log.info("Telemetry engine job scheduled: interval={}s", telemetryScanIntervalSeconds);
  }
}
