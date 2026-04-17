package com.iviet.ivshs.startup;

import com.iviet.ivshs.schedule.energy.EnergyMetricResetJob;
import com.iviet.ivshs.schedule.energy.EnergyMetricTelemetryJob;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
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

@Slf4j(topic = "ENERGY-INIT")
@Component
@Order(35)
@RequiredArgsConstructor
public class EnergyMetricInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final SchedulerFactoryBean schedulerFactoryBean;
    private final Environment env;

    private boolean isInitialized = false;
    private int     telemetryIntervalSeconds;
    private String  resetCron;

    @PostConstruct
    private void init() {
        telemetryIntervalSeconds = env.getProperty("app.engine.energy.telemetry.intervalSeconds", Integer.class, 300);
        resetCron                = env.getProperty("app.engine.energy.reset.cron", String.class, "0 0 0 * * ?");
        log.info("Configured with telemetry interval: {}s, reset cron: {}", 
            telemetryIntervalSeconds, resetCron);
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (isInitialized) return;

        try {
            log.info("Module: [Energy Metric Engine] -> [RUNNING]");
            long start = System.currentTimeMillis();

            scheduleTelemetryJob();
            scheduleResetJob();

            log.info("Module: [Energy Metric Engine] -> [OK]");
            log.info("Duration: {} ms", System.currentTimeMillis() - start);
            isInitialized = true;

        } catch (Exception e) {
            log.error("Module: [Energy Metric Engine] -> [FAILED]");
            log.error("Reason: {}", e.getMessage(), e);
            log.warn("WARNING: Server proceeding without Energy Metric Engine");
        }
    }

    private void scheduleTelemetryJob() throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        JobDetail jobDetail = JobBuilder.newJob(EnergyMetricTelemetryJob.class)
            .withIdentity(EnergyMetricTelemetryJob.JOB_NAME, EnergyMetricTelemetryJob.JOB_GROUP)
            .storeDurably()
            .build();

        if (scheduler.checkExists(jobDetail.getKey())) {
            scheduler.deleteJob(jobDetail.getKey());
        }

        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("EnergyMetricTelemetryTrigger", EnergyMetricTelemetryJob.JOB_GROUP)
            .startNow()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(telemetryIntervalSeconds)
                .repeatForever())
            .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("[EnergyMetricTelemetryJob] scheduled every {}s", telemetryIntervalSeconds);
    }

    private void scheduleResetJob() throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        JobDetail jobDetail = JobBuilder.newJob(EnergyMetricResetJob.class)
            .withIdentity(EnergyMetricResetJob.JOB_NAME, EnergyMetricResetJob.JOB_GROUP)
            .storeDurably()
            .build();

        if (scheduler.checkExists(jobDetail.getKey())) {
            scheduler.deleteJob(jobDetail.getKey());
        }

        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("EnergyMetricResetTrigger", EnergyMetricResetJob.JOB_GROUP)
            .withSchedule(CronScheduleBuilder.cronSchedule(resetCron))
            .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("[EnergyMetricResetJob] scheduled at [{}] UTC daily", resetCron);
    }
}
