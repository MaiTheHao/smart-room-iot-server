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

    public void sync(BaseSchedulableEntity entity) {
        JobKey jobKey = JobKey.jobKey(entity.getJobName(), entity.getJobGroup());
        TriggerKey triggerKey = TriggerKey.triggerKey("Trigger_" + entity.getJobName(), entity.getJobGroup());

        if (Boolean.FALSE.equals(entity.getIsActive())) {
            quartzUtil.deleteJob(jobKey);
            return;
        }

        Trigger trigger = buildTrigger(entity, triggerKey);

        if (quartzUtil.checkExists(triggerKey)) {
            quartzUtil.rescheduleJob(triggerKey, trigger);
        } else {
            JobDetail jobDetail = JobBuilder.newJob(entity.getJobClass())
                    .withIdentity(jobKey)
                    .usingJobData(entity.getJobDataMap())
                    .storeDurably()
                    .build();
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

    private Trigger buildTrigger(BaseSchedulableEntity entity, TriggerKey triggerKey) {
        TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger().withIdentity(triggerKey);

        if (Boolean.TRUE.equals(entity.getIsInterval())) {
            validateInterval(entity);
            return builder.withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(entity.getIntervalSeconds())
                            .repeatForever()
                            .withMisfireHandlingInstructionNextWithExistingCount())
                    .build();
        }

        validateCron(entity);
        return builder.withSchedule(CronScheduleBuilder.cronSchedule(entity.getCronExpression())
                        .inTimeZone(TimeZone.getTimeZone(appTimezone))
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

    public void deleteJobGroup(String groupName) {
        quartzUtil.deleteJobsInGroup(groupName);
    }
}
