package com.iviet.ivshs.startup;

import java.util.List;

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
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.schedule.metric.MetricJobProvider;
import com.iviet.ivshs.schedule.metric.MetricJobRegistration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "METRIC-INIT")
@Component
@Order(35)
@RequiredArgsConstructor
public class MetricSystemInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final SchedulerFactoryBean schedulerFactoryBean;
    private final List<MetricJobProvider> jobProviders;

    private boolean isInitialized = false;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (isInitialized) return;

        try {
            log.info("Module: [Metric System Scheduler] -> [RUNNING]");
            long start = System.currentTimeMillis();
            
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            
            int jobCount = 0;
            for (MetricJobProvider provider : jobProviders) {
                List<MetricJobRegistration> jobs = provider.getMetricJobs();
                if (jobs != null && !jobs.isEmpty()) {
                    for (MetricJobRegistration jobReg : jobs) {
                        scheduleJob(scheduler, jobReg);
                        jobCount++;
                    }
                }
            }

            log.info("Module: [Metric System Scheduler] -> [OK] (Scheduled {} jobs)", jobCount);
            log.info("Duration: {} ms", System.currentTimeMillis() - start);
            isInitialized = true;

        } catch (Exception e) {
            log.error("Module: [Metric System Scheduler] -> [FAILED]");
            log.error("Reason: {}", e.getMessage(), e);
            log.warn("WARNING: Server proceeding without some Metric Jobs");
        }
    }

    private void scheduleJob(Scheduler scheduler, MetricJobRegistration jobReg) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(jobReg.getJobClass())
            .withIdentity(jobReg.getName(), jobReg.getGroup())
            .storeDurably()
            .build();

        if (scheduler.checkExists(jobDetail.getKey())) {
            scheduler.deleteJob(jobDetail.getKey());
        }

        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
            .withIdentity(jobReg.getName() + "Trigger", jobReg.getGroup());

        if (jobReg.getCronExpression() != null && !jobReg.getCronExpression().isBlank()) {
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(jobReg.getCronExpression()));
            log.info("[{}] scheduled with CRON [{}]", jobReg.getName(), jobReg.getCronExpression());
        } else if (jobReg.getIntervalSeconds() != null && jobReg.getIntervalSeconds() > 0) {
            triggerBuilder.startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(jobReg.getIntervalSeconds())
                    .repeatForever());
            log.info("[{}] scheduled every {}s", jobReg.getName(), jobReg.getIntervalSeconds());
        } else {
            log.warn("[{}] skipped: No valid CRON or Interval configured", jobReg.getName());
            return;
        }

        scheduler.scheduleJob(jobDetail, triggerBuilder.build());
    }
}
