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

import com.iviet.ivshs.core.properties.EngineProperties;
import com.iviet.ivshs.scheduler.system.metric.MetricJobProvider;
import com.iviet.ivshs.scheduler.system.metric.MetricJobRegistration;
import com.iviet.ivshs.scheduler.system.telemetry.TelemetryJob;
import com.iviet.ivshs.service.automation.AutomationService;
import com.iviet.ivshs.service.rule.RuleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(998)
@RequiredArgsConstructor
public class SchedulerInitializer implements ApplicationListener<ContextRefreshedEvent> {

	private final AutomationService automationService;
	private final RuleService ruleService;
	private final SchedulerFactoryBean schedulerFactoryBean;
	private final List<MetricJobProvider> jobProviders;
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
		if (isInitialized) {
			return;
		}

		long totalStart = System.currentTimeMillis();

		// 1. Initialize Automation Engine
		String autoStatus = "FAILED";
		try {
			long startTime = System.currentTimeMillis();
			automationService.reloadAll();
			long duration = System.currentTimeMillis() - startTime;
			autoStatus = "SUCCESS (" + duration + "ms)";
		} catch (Exception e) {
			log.error("Automation Engine initialization failed", e);
		}

		// 2. Initialize Rule Engine
		String ruleStatus = "FAILED";
		try {
			long startTime = System.currentTimeMillis();
			ruleService.reloadAll();
			long duration = System.currentTimeMillis() - startTime;
			ruleStatus = "SUCCESS (" + duration + "ms)";
		} catch (Exception e) {
			log.error("Rule Engine initialization failed", e);
		}

		// 3. Initialize Telemetry Engine
		String teleStatus = "SKIPPED (scanIntervalSeconds <= 0)";
		if (telemetryScanIntervalSeconds > 0) {
			try {
				long startTime = System.currentTimeMillis();
				scheduleGlobalTelemetryJob();
				long duration = System.currentTimeMillis() - startTime;
				teleStatus = "SUCCESS (" + duration + "ms, interval=" + telemetryScanIntervalSeconds + "s)";
			} catch (Exception e) {
				log.error("Telemetry Engine initialization failed", e);
				teleStatus = "FAILED";
			}
		}

		// 4. Initialize Metric System Scheduler
		String metricStatus = "FAILED";
		try {
			long startTime = System.currentTimeMillis();
			Scheduler scheduler = schedulerFactoryBean.getScheduler();
			int jobCount = 0;
			for (MetricJobProvider provider : jobProviders) {
				List<MetricJobRegistration> jobs = provider.getMetricJobs();
				if (jobs != null && !jobs.isEmpty()) {
					for (MetricJobRegistration jobReg : jobs) {
						scheduleMetricJob(scheduler, jobReg);
						jobCount++;
					}
				}
			}
			long duration = System.currentTimeMillis() - startTime;
			metricStatus = "SUCCESS (" + duration + "ms, jobCount=" + jobCount + ")";
		} catch (Exception e) {
			log.error("Metric System Scheduler initialization failed", e);
		}

		long totalDuration = System.currentTimeMillis() - totalStart;

		log.info("------------------------------------------------------------");
		log.info("SCHEDULER ENGINE INITIALIZATION");
		log.info("------------------------------------------------------------");
		log.info("Automation   : {}", autoStatus);
		log.info("Rule         : {}", ruleStatus);
		log.info("Telemetry    : {}", teleStatus);
		log.info("Metric       : {}", metricStatus);
		log.info("Status       : INITIALIZED in {}ms", totalDuration);
		log.info("------------------------------------------------------------");

		isInitialized = true;
	}

	private void scheduleGlobalTelemetryJob() throws SchedulerException {
		Scheduler scheduler = schedulerFactoryBean.getScheduler();

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
				.withSchedule(
						SimpleScheduleBuilder.simpleSchedule()
								.withIntervalInSeconds(telemetryScanIntervalSeconds)
								.repeatForever())
				.build();

		scheduler.scheduleJob(jobDetail, trigger);
		log.info("Telemetry engine job scheduled: interval={}s", telemetryScanIntervalSeconds);
	}

	private void scheduleMetricJob(Scheduler scheduler, MetricJobRegistration jobReg) throws SchedulerException {
		JobDetail jobDetail = JobBuilder.newJob(jobReg.getJobClass())
				.withIdentity(jobReg.getName(), jobReg.getGroup())
				.storeDurably()
				.build();

		if (scheduler.checkExists(jobDetail.getKey())) {
			scheduler.deleteJob(jobDetail.getKey());
		}

		TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
				.withIdentity(jobReg.getName() + "Trigger", jobReg.getGroup());

		if (jobReg.getCronExpression() != null && !jobReg.getCronExpression()
				.isBlank()) {
			triggerBuilder.withSchedule(
					CronScheduleBuilder.cronSchedule(jobReg.getCronExpression())
							.inTimeZone(TimeZone.getTimeZone("UTC")));
			log.info("Scheduled metric job: name={}, cron={}, timezone=UTC", jobReg.getName(), jobReg.getCronExpression());
		} else if (jobReg.getIntervalSeconds() != null && jobReg.getIntervalSeconds() > 0) {
			triggerBuilder.startNow()
					.withSchedule(
							SimpleScheduleBuilder.simpleSchedule()
									.withIntervalInSeconds(jobReg.getIntervalSeconds())
									.repeatForever());
			log.info("Scheduled metric job: name={}, interval={}s", jobReg.getName(), jobReg.getIntervalSeconds());
		} else {
			log.warn("Skipped metric job configuration: name={}, reason=no valid CRON or interval configured", jobReg.getName());
			return;
		}

		scheduler.scheduleJob(jobDetail, triggerBuilder.build());
	}
}
