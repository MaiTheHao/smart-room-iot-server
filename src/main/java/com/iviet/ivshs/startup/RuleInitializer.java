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

import com.iviet.ivshs.schedule.job.RuleEngineJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Startup")
@Component
@Order(20)
@RequiredArgsConstructor
public class RuleInitializer implements ApplicationListener<ContextRefreshedEvent> {

  private final SchedulerFactoryBean schedulerFactoryBean;
  private final Environment env;
  private boolean isInitialized = false;

  @Override
  public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
    if (isInitialized) {
      return;
    }

    try {
      log.info("Module       : [Rule Engine] -> [RUNNING]");
      
      long startTime = System.currentTimeMillis();
      scheduleGlobalRuleEngineJob();
      long duration = System.currentTimeMillis() - startTime;

      log.info("Module       : [Rule Engine] -> [OK]");
      log.info("  - Duration   : {} ms", duration);

      isInitialized = true;

    } catch (Exception e) {
      log.error("Module       : [Rule Engine] -> [FAILED]");
      log.error("  - Reason     : {}", e.getMessage());
      log.error("------------------------------------------------------------");
      log.error("Stack trace:", e);
      log.warn("WARNING: Server proceeding without rule engine");
      log.warn("ACTION: Check logs and restart server if needed");
    }
  }

  private void scheduleGlobalRuleEngineJob() throws SchedulerException {
    Scheduler scheduler = schedulerFactoryBean.getScheduler();
    
    Integer scanIntervalSeconds = env.getProperty("app.engine.rule.scanIntervalSeconds", Integer.class, 300);

    log.info("Scheduling Rule Engine Job with interval: {} seconds", scanIntervalSeconds);

    JobDetail jobDetail = JobBuilder.newJob(RuleEngineJob.class)
        .withIdentity(RuleEngineJob.JOB_NAME, RuleEngineJob.JOB_GROUP)
        .storeDurably()
        .build();

    if (scheduler.checkExists(jobDetail.getKey())) {
      scheduler.deleteJob(jobDetail.getKey());
    }

    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("RuleEngineTrigger", RuleEngineJob.JOB_GROUP)
        .startNow()
        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInSeconds(scanIntervalSeconds)
            .repeatForever())
        .build();

    scheduler.scheduleJob(jobDetail, trigger);
    log.info("Rule Engine Job scheduled successfully (Interval: {}s).", scanIntervalSeconds);
  }
}
