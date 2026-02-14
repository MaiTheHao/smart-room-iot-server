package com.iviet.ivshs.util;

import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzUtil {

    private final Scheduler scheduler;

    /**
     * Schedule a new job with a specific trigger.
     */
    public Date scheduleJob(JobDetail jobDetail, Trigger trigger) {
        try {
            Date date = scheduler.scheduleJob(jobDetail, trigger);
            log.info("Scheduled job: {} with trigger: {}", jobDetail.getKey(), trigger.getKey());
            return date;
        } catch (SchedulerException e) {
            log.error("Error scheduling job '{}': {}", jobDetail.getKey(), e.getMessage());
            throw new InternalServerErrorException("Failed to schedule job: " + jobDetail.getKey());
        }
    }

    /**
     * Reschedule an existing job with a new trigger.
     */
    public Date rescheduleJob(TriggerKey triggerKey, Trigger newTrigger) {
        try {
            Date date = scheduler.rescheduleJob(triggerKey, newTrigger);
            log.info("Rescheduled trigger: {}. Next run: {}", triggerKey, date);
            return date;
        } catch (SchedulerException e) {
            log.error("Error rescheduling trigger '{}': {}", triggerKey, e.getMessage());
            throw new InternalServerErrorException("Failed to reschedule job");
        }
    }

    /**
     * Delete a job.
     */
    public void deleteJob(JobKey jobKey) {
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                log.info("Deleted job: {}", jobKey);
            }
        } catch (SchedulerException e) {
            log.error("Error deleting job '{}': {}", jobKey, e.getMessage());
            throw new InternalServerErrorException("Failed to delete job");
        }
    }

    /**
     * Delete all jobs in a group.
     */
    public void deleteJobsInGroup(String groupName) {
        try {
            GroupMatcher<JobKey> matcher = GroupMatcher.groupEquals(groupName);
            Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);

            if (jobKeys != null && !jobKeys.isEmpty()) {
                scheduler.deleteJobs(new java.util.ArrayList<>(jobKeys));
                log.info("Deleted {} jobs in group '{}'", jobKeys.size(), groupName);
            }
        } catch (SchedulerException e) {
            log.error("Error deleting group '{}': {}", groupName, e.getMessage());
        }
    }

    /**
     * Pause a job.
     */
    public void pauseJob(JobKey jobKey) {
        try {
            scheduler.pauseJob(jobKey);
            log.info("Paused job: {}", jobKey);
        } catch (SchedulerException e) {
            throw new InternalServerErrorException("Failed to pause job");
        }
    }

    /**
     * Resume a job.
     */
    public void resumeJob(JobKey jobKey) {
        try {
            scheduler.resumeJob(jobKey);
            log.info("Resumed job: {}", jobKey);
        } catch (SchedulerException e) {
            throw new InternalServerErrorException("Failed to resume job");
        }
    }

    /**
     * Trigger a job immediately.
     */
    public void triggerJob(JobKey jobKey, JobDataMap dataMap) {
        try {
            scheduler.triggerJob(jobKey, dataMap);
            log.info("Manually triggered job: {}", jobKey);
        } catch (SchedulerException e) {
            log.error("Error triggering job '{}': {}", jobKey, e.getMessage());
            throw new InternalServerErrorException("Failed to trigger job manually");
        }
    }

    public boolean checkExists(JobKey jobKey) {
        try {
            return scheduler.checkExists(jobKey);
        } catch (SchedulerException e) {
            log.error("Error checking existence of job '{}': {}", jobKey, e.getMessage());
            return false;
        }
    }

    public boolean checkExists(TriggerKey triggerKey) {
        try {
            return scheduler.checkExists(triggerKey);
        } catch (SchedulerException e) {
            log.error("Error checking existence of trigger '{}': {}", triggerKey, e.getMessage());
            return false;
        }
    }
}
