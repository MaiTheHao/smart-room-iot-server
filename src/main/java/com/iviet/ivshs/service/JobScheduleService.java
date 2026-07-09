package com.iviet.ivshs.service;

import com.iviet.ivshs.entities.base.BaseSchedulableEntity;
import com.iviet.ivshs.shared.exception.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobScheduleService {

    private final Scheduler scheduler;

    public void sync(BaseSchedulableEntity entity) {
        JobKey jobKey = JobKey.jobKey(entity.getJobName(), entity.getJobGroup());
        TriggerKey triggerKey = TriggerKey.triggerKey("Trigger_" + entity.getJobName(), entity.getJobGroup());

        try {
            if (Boolean.FALSE.equals(entity.getIsActive())) {
                deleteJobByKey(jobKey);
                return;
            }

            Trigger trigger = buildTrigger(entity, triggerKey);

            if (scheduler.checkExists(triggerKey)) {
                Date nextRun = scheduler.rescheduleJob(triggerKey, trigger);
                log.info("Rescheduled job: triggerKey={}, nextRun={}", triggerKey, formatUtc(nextRun));
            } else {
                JobDetail jobDetail = JobBuilder.newJob(entity.getJobClass())
                        .withIdentity(jobKey)
                        .usingJobData(entity.getJobDataMap())
                        .storeDurably()
                        .build();
                Date nextRun = scheduler.scheduleJob(jobDetail, trigger);
                log.info("Scheduled new job: jobKey={}, nextRun={}", jobKey, formatUtc(nextRun));
            }
        } catch (SchedulerException e) {
            log.error("Failed to sync job: jobKey={}", jobKey, e);
            throw new InternalServerErrorException("Failed to sync job: " + entity.getJobName());
        }
    }

    public void delete(BaseSchedulableEntity entity) {
        deleteJobByKey(JobKey.jobKey(entity.getJobName(), entity.getJobGroup()));
    }

    public void deleteJobGroup(String groupName) {
        try {
            GroupMatcher<JobKey> matcher = GroupMatcher.groupEquals(groupName);
            Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);

            if (jobKeys != null && !jobKeys.isEmpty()) {
                scheduler.deleteJobs(new java.util.ArrayList<>(jobKeys));
                log.info("Deleted jobs in group: count={}, group={}", jobKeys.size(), groupName);
            }
        } catch (SchedulerException e) {
            log.error("Failed to delete jobs in group: {}", groupName, e);
            throw new InternalServerErrorException("Failed to delete jobs in group: " + groupName);
        }
    }

    public void pause(BaseSchedulableEntity entity) {
        JobKey jobKey = JobKey.jobKey(entity.getJobName(), entity.getJobGroup());
        try {
            scheduler.pauseJob(jobKey);
            log.info("Paused job: {}", jobKey);
        } catch (SchedulerException e) {
            log.error("Failed to pause job: {}", jobKey, e);
            throw new InternalServerErrorException("Failed to pause job: " + entity.getJobName());
        }
    }

    public void resume(BaseSchedulableEntity entity) {
        JobKey jobKey = JobKey.jobKey(entity.getJobName(), entity.getJobGroup());
        try {
            scheduler.resumeJob(jobKey);
            log.info("Resumed job: {}", jobKey);
        } catch (SchedulerException e) {
            log.error("Failed to resume job: {}", jobKey, e);
            throw new InternalServerErrorException("Failed to resume job: " + entity.getJobName());
        }
    }

    public void triggerNow(BaseSchedulableEntity entity) {
        JobKey jobKey = JobKey.jobKey(entity.getJobName(), entity.getJobGroup());
        try {
            scheduler.triggerJob(jobKey, entity.getJobDataMap());
            log.info("Manually triggered job: {}", jobKey);
        } catch (SchedulerException e) {
            log.error("Failed to trigger job manually: {}", jobKey, e);
            throw new InternalServerErrorException("Failed to trigger job manually: " + entity.getJobName());
        }
    }

    // --- Private Helper Methods ---

    private void deleteJobByKey(JobKey jobKey) {
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                log.info("Deleted job: {}", jobKey);
            }
        } catch (SchedulerException e) {
            log.error("Failed to delete job: {}", jobKey, e);
            throw new InternalServerErrorException("Failed to delete job: " + jobKey.getName());
        }
    }

    private Trigger buildTrigger(BaseSchedulableEntity entity, TriggerKey triggerKey) {
        TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey);

        if (Boolean.TRUE.equals(entity.getIsInterval())) {
            validateInterval(entity);
            return builder.withSchedule(
                    SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(entity.getIntervalSeconds())
                            .repeatForever()
                            .withMisfireHandlingInstructionNextWithExistingCount())
                    .build();
        }

        validateCron(entity);
        return builder.withSchedule(
                CronScheduleBuilder.cronSchedule(entity.getCronExpression())
                        .inTimeZone(TimeZone.getTimeZone("UTC"))
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();
    }

    private void validateInterval(BaseSchedulableEntity entity) {
        if (entity.getIntervalSeconds() == null || entity.getIntervalSeconds() <= 0) {
            log.error("Invalid Interval Seconds for job '{}': {}", entity.getJobName(), entity.getIntervalSeconds());
            throw new InternalServerErrorException("Invalid Interval Seconds: " + entity.getIntervalSeconds());
        }
    }

    private void validateCron(BaseSchedulableEntity entity) {
        if (!CronExpression.isValidExpression(entity.getCronExpression())) {
            log.error("Invalid Cron Expression for job '{}': {}", entity.getJobName(), entity.getCronExpression());
            throw new InternalServerErrorException("Invalid Cron Expression: " + entity.getCronExpression());
        }
    }

    private String formatUtc(Date date) {
        return date != null ? date.toInstant()
                .toString() : "null";
    }
}
