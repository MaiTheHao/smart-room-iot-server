package com.iviet.ivshs.core.startup;

import java.util.List;
import java.util.TimeZone;

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
import com.iviet.ivshs.scheduler.metric.MetricJobProvider;
import com.iviet.ivshs.scheduler.metric.MetricJobRegistration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(35)
@RequiredArgsConstructor
public class MetricSystemInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final SchedulerFactoryBean schedulerFactoryBean;
    private final List<MetricJobProvider> jobProviders;

    private boolean isInitialized = false;

    @Override
    public void onApplicationEvent(@NonNull
    ContextRefreshedEvent event) {
        if (isInitialized)
            return;

        try {
            log.info("Starting Metric System Scheduler initialization");
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

            log.info("Metric System Scheduler initialized successfully: scheduledJobCount={}, duration={}ms", jobCount, System.currentTimeMillis() - start);
            isInitialized = true;

        } catch (Exception e) {
            log.error("Metric System Scheduler initialization failed", e);
            log.warn("Server proceeding without Metric System Scheduler. Please check database/Quartz scheduler status.");
        }
    }

    private void scheduleJob(Scheduler scheduler, MetricJobRegistration jobReg) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(jobReg.getJobClass()).withIdentity(jobReg.getName(), jobReg.getGroup()).storeDurably().build();

        if (scheduler.checkExists(jobDetail.getKey())) {
            scheduler.deleteJob(jobDetail.getKey());
        }

        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger().withIdentity(jobReg.getName() + "Trigger", jobReg.getGroup());

        if (jobReg.getCronExpression() != null && !jobReg.getCronExpression().isBlank()) {
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(jobReg.getCronExpression()).inTimeZone(TimeZone.getTimeZone("UTC")));
            log.info("Scheduled metric job: name={}, cron={}, timezone=UTC", jobReg.getName(), jobReg.getCronExpression());
        } else if (jobReg.getIntervalSeconds() != null && jobReg.getIntervalSeconds() > 0) {
            triggerBuilder.startNow().withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(jobReg.getIntervalSeconds()).repeatForever());
            log.info("Scheduled metric job: name={}, interval={}s", jobReg.getName(), jobReg.getIntervalSeconds());
        } else {
            log.warn("Skipped metric job configuration: name={}, reason=no valid CRON or interval configured", jobReg.getName());
            return;
        }

        scheduler.scheduleJob(jobDetail, triggerBuilder.build());
    }
}
