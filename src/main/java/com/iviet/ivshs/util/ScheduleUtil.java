package com.iviet.ivshs.util;

import com.iviet.ivshs.entities.BaseSchedulableEntity;
import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.TimeZone;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleUtil {

    private final QuartzUtil quartzUtil;

    @Value("${app.timezone:Asia/Ho_Chi_Minh}")
    private String appTimezone;

    /**
     * Syncs the entity's schedule with the Quartz scheduler.
     * Logic:
     * 1. If inactive -> Delete job.
     * 2. If valid cron -> Schedule or Reschedule.
     */
    public void sync(BaseSchedulableEntity entity) {
        String jobName = entity.getJobName();
        String jobGroup = entity.getJobGroup();

        if (Boolean.FALSE.equals(entity.getIsActive())) {
            quartzUtil.deleteJob(JobKey.jobKey(jobName, jobGroup));
            return;
        }

        if (!CronExpression.isValidExpression(entity.getCronExpression())) {
            log.error("Invalid Cron Expression for job '{}': {}", jobName, entity.getCronExpression());
            throw new InternalServerErrorException("Invalid Cron Expression: " + entity.getCronExpression());
        }

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        TriggerKey triggerKey = TriggerKey.triggerKey("Trigger_" + jobName, jobGroup);

        if (quartzUtil.checkExists(triggerKey)) {
            // Update existing trigger
            Trigger newTrigger = buildCronTrigger(entity, triggerKey);
            quartzUtil.rescheduleJob(triggerKey, newTrigger);
        } else {
            // Create new job
            JobDetail jobDetail = JobBuilder.newJob(entity.getJobClass())
                    .withIdentity(jobKey)
                    .withDescription("Scheduled by ScheduleUtil")
                    .usingJobData(entity.getJobDataMap())
                    .storeDurably()
                    .build();

            Trigger trigger = buildCronTrigger(entity, triggerKey);
            quartzUtil.scheduleJob(jobDetail, trigger);
        }
    }

    public void delete(BaseSchedulableEntity entity) {
        quartzUtil.deleteJob(JobKey.jobKey(entity.getJobName(), entity.getJobGroup()));
    }

    public void pause(BaseSchedulableEntity entity) {
        quartzUtil.pauseJob(JobKey.jobKey(entity.getJobName(), entity.getJobGroup()));
    }

    public void resume(BaseSchedulableEntity entity) {
        quartzUtil.resumeJob(JobKey.jobKey(entity.getJobName(), entity.getJobGroup()));
    }

    public void triggerNow(BaseSchedulableEntity entity) {
        quartzUtil.triggerJob(JobKey.jobKey(entity.getJobName(), entity.getJobGroup()), entity.getJobDataMap());
    }

    private Trigger buildCronTrigger(BaseSchedulableEntity entity, TriggerKey triggerKey) {
        return TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(CronScheduleBuilder.cronSchedule(entity.getCronExpression())
                        .inTimeZone(TimeZone.getTimeZone(appTimezone))
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();
    }

    public void deleteJobGroup(String groupName) {
        quartzUtil.deleteJobsInGroup(groupName);
    }
}
