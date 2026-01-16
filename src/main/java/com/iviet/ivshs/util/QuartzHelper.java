package com.iviet.ivshs.util;

import com.iviet.ivshs.entities.Automation;
import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import com.iviet.ivshs.job.AutomationJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzHelper {

    private final Scheduler scheduler;

    private static final String JOB_GROUP = "AUTOMATION_GROUP";
    private static final String TRIGGER_GROUP = "AUTOMATION_TRIGGER_GROUP";
    private static final String JOB_PREFIX = "Auto_";
    private static final String TRIGGER_PREFIX = "Trigger_";

    public void schedule(Automation automation) {
        JobKey jobKey = getJobKey(automation.getId());
        try {
            if (scheduler.checkExists(jobKey)) {
                log.warn("Job already exists, skipping schedule: {}", jobKey);
                return;
            }

            JobDetail jobDetail = JobBuilder.newJob(AutomationJob.class)
                    .withIdentity(jobKey)
                    .usingJobData("id", automation.getId())
                    .storeDurably(false)
                    .build();

            Trigger trigger = buildCronTrigger(automation);

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Successfully scheduled automation [ID: {}] with cron [{}]", automation.getId(), automation.getCronExpression());

        } catch (SchedulerException e) {
            log.error("Error scheduling automation [ID: {}]: {}", automation.getId(), e.getMessage(), e);
            throw new InternalServerErrorException("Quartz error: Failed to schedule job");
        }
    }

    public void sync(Automation automation) {
        try {
            if (Boolean.TRUE.equals(automation.getIsActive())) {
                TriggerKey triggerKey = getTriggerKey(automation.getId());
                
                if (scheduler.checkExists(triggerKey)) {
                    reschedule(automation, triggerKey);
                } else {
                    log.debug("Trigger not found for active automation [ID: {}], creating new...", automation.getId());
                    schedule(automation);
                }
            } else {
                log.debug("Automation [ID: {}] is inactive, ensuring job is deleted", automation.getId());
                delete(automation.getId());
            }
        } catch (SchedulerException e) {
            log.error("Error syncing automation [ID: {}]: {}", automation.getId(), e.getMessage(), e);
            throw new InternalServerErrorException("Quartz error: Failed to sync automation state");
        }
    }

    public void delete(Long automationId) {
        JobKey jobKey = getJobKey(automationId);
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                log.info("Successfully deleted job: {}", jobKey);
            } else {
                log.debug("Job {} does not exist, nothing to delete", jobKey);
            }
        } catch (SchedulerException e) {
            log.error("Error deleting job [ID: {}]: {}", automationId, e.getMessage(), e);
            throw new InternalServerErrorException("Quartz error: Failed to delete job");
        }
    }

    public void deleteAllInGroup() {
        try {
            scheduler.getJobKeys(GroupMatcher.groupEquals(JOB_GROUP))
                    .forEach(jobKey -> {
                        try {
                            scheduler.deleteJob(jobKey);
                            log.debug("Deleted job from group: {}", jobKey);
                        } catch (SchedulerException e) {
                            log.error("Failed to delete job {} during mass deletion", jobKey, e);
                        }
                    });
            log.info("Completed cleaning up all jobs in group: {}", JOB_GROUP);
        } catch (SchedulerException e) {
            log.error("Failed to fetch jobs for group deletion", e);
        }
    }

    private void reschedule(Automation automation, TriggerKey triggerKey) throws SchedulerException {
        Trigger newTrigger = buildCronTrigger(automation);
        scheduler.rescheduleJob(triggerKey, newTrigger);
        log.info("Rescheduled automation [ID: {}] with new cron [{}]", 
                automation.getId(), automation.getCronExpression());
    }

    private Trigger buildCronTrigger(Automation automation) {
        return TriggerBuilder.newTrigger()
                .withIdentity(getTriggerKey(automation.getId()))
                .withSchedule(CronScheduleBuilder.cronSchedule(automation.getCronExpression())
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    private JobKey getJobKey(Long id) {
        return JobKey.jobKey(JOB_PREFIX + id, JOB_GROUP);
    }

    private TriggerKey getTriggerKey(Long id) {
        return TriggerKey.triggerKey(TRIGGER_PREFIX + id, TRIGGER_GROUP);
    }
}