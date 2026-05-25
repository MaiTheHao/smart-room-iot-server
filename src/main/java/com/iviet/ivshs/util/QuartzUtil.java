package com.iviet.ivshs.util;

import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Component;

import java.util.Date;
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
            log.info("Scheduled job: jobKey={}, triggerKey={}, nextRun={}", jobDetail.getKey(), trigger.getKey(), formatUtc(date));
            return date;
        } catch (SchedulerException e) {
            log.error("Failed to schedule job: jobKey={}, triggerKey={}", jobDetail.getKey(), trigger.getKey(), e);
            throw new InternalServerErrorException("Failed to schedule job: " + jobDetail.getKey());
        }
    }

    /**
     * Reschedule an existing job with a new trigger.
     */
    public Date rescheduleJob(TriggerKey triggerKey, Trigger newTrigger) {
        try {
            Date date = scheduler.rescheduleJob(triggerKey, newTrigger);
            log.info("Rescheduled job trigger: triggerKey={}, nextRun={}", triggerKey, formatUtc(date));
            return date;
        } catch (SchedulerException e) {
            log.error("Failed to reschedule job trigger: triggerKey={}", triggerKey, e);
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
                log.info("Deleted job: jobKey={}", jobKey);
            } else {
                log.warn("Job to delete does not exist: jobKey={}", jobKey);
            }
        } catch (SchedulerException e) {
            log.error("Failed to delete job: jobKey={}", jobKey, e);
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
                log.info("Deleted jobs in group: count={}, group={}", jobKeys.size(), groupName);
            } else {
                log.info("No jobs found to delete in group: group={}", groupName);
            }
        } catch (SchedulerException e) {
            log.error("Failed to delete jobs in group: group={}", groupName, e);
        }
    }

    /**
     * Pause a job.
     */
    public void pauseJob(JobKey jobKey) {
        try {
            scheduler.pauseJob(jobKey);
            log.info("Paused job: jobKey={}", jobKey);
        } catch (SchedulerException e) {
            log.error("Failed to pause job: jobKey={}", jobKey, e);
            throw new InternalServerErrorException("Failed to pause job");
        }
    }

    /**
     * Resume a job.
     */
    public void resumeJob(JobKey jobKey) {
        try {
            scheduler.resumeJob(jobKey);
            log.info("Resumed job: jobKey={}", jobKey);
        } catch (SchedulerException e) {
            log.error("Failed to resume job: jobKey={}", jobKey, e);
            throw new InternalServerErrorException("Failed to resume job");
        }
    }

    /**
     * Trigger a job immediately.
     */
    public void triggerJob(JobKey jobKey, JobDataMap dataMap) {
        try {
            scheduler.triggerJob(jobKey, dataMap);
            log.info("Manually triggered job: jobKey={}", jobKey);
        } catch (SchedulerException e) {
            log.error("Failed to trigger job manually: jobKey={}", jobKey, e);
            throw new InternalServerErrorException("Failed to trigger job manually");
        }
    }

    public boolean checkExists(JobKey jobKey) {
        try {
            return scheduler.checkExists(jobKey);
        } catch (SchedulerException e) {
            log.error("Failed to check existence of job: jobKey={}", jobKey, e);
            return false;
        }
    }

    public boolean checkExists(TriggerKey triggerKey) {
        try {
            return scheduler.checkExists(triggerKey);
        } catch (SchedulerException e) {
            log.error("Failed to check existence of trigger: triggerKey={}", triggerKey, e);
            return false;
        }
    }

    private String formatUtc(Date date) {
        return date != null ? date.toInstant().toString() : "null";
    }
}
