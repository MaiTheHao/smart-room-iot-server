package com.iviet.ivshs.util;

import com.iviet.ivshs.entities.BaseSchedulableEntity;
import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.TimeZone;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzHelper {

    private final Scheduler scheduler;

    @Value("${app.timezone:Asia/Ho_Chi_Minh}")
    private String appTimezone;

    /**
     * Đồng bộ Job: Tự động quyết định Schedule mới, Reschedule, hoặc Delete dựa trên trạng thái entity.
     */
    public void sync(BaseSchedulableEntity entity) {
        String jobName = entity.getJobName();
        try {
            // 1. Nếu tắt (isActive = false) -> Xóa luôn cho sạch
            if (Boolean.FALSE.equals(entity.getIsActive())) {
                delete(entity);
                return;
            }

            // 2. Validate Cron Expression (Chặn lỗi cú pháp ngay từ đầu)
            if (!CronExpression.isValidExpression(entity.getCronExpression())) {
                log.error("Invalid Cron Expression for job '{}': {}", jobName, entity.getCronExpression());
                throw new InternalServerErrorException("Invalid Cron Expression: " + entity.getCronExpression());
            }

            // 3. Kiểm tra tồn tại để Create hay Update
            TriggerKey triggerKey = getTriggerKey(entity);
            if (scheduler.checkExists(triggerKey)) {
                rescheduleJob(entity, triggerKey);
            } else {
                scheduleNewJob(entity);
            }

        } catch (SchedulerException e) {
            log.error("Error syncing job '{}': {}", jobName, e.getMessage(), e);
            throw new InternalServerErrorException("Failed to sync job: " + jobName);
        }
    }

    /**
     * Tạo Job mới
     */
    private void scheduleNewJob(BaseSchedulableEntity entity) throws SchedulerException {
        JobKey jobKey = getJobKey(entity);
        
        // Double check tránh trùng
        if (scheduler.checkExists(jobKey)) {
            log.warn("Job already exists, skipping create: {}", jobKey);
            return;
        }

        JobDetail jobDetail = JobBuilder.newJob(entity.getJobClass())
                .withIdentity(jobKey)
                .withDescription("Scheduled by QuartzHelper")
                .usingJobData(entity.getJobDataMap())
                .storeDurably() // Quan trọng: Giữ Job lại kể cả khi Trigger bị xóa
                .build();

        scheduler.scheduleJob(jobDetail, buildCronTrigger(entity));
        log.info("Scheduled NEW job: [{} - {}]", entity.getJobGroup(), entity.getJobName());
    }

    /**
     * Cập nhật lại Trigger cho Job đã tồn tại
     */
    private void rescheduleJob(BaseSchedulableEntity entity, TriggerKey triggerKey) throws SchedulerException {
        Trigger newTrigger = buildCronTrigger(entity);
        Date nextFireTime = scheduler.rescheduleJob(triggerKey, newTrigger);
        log.info("Rescheduled job: [{}]. Next run: {}", entity.getJobName(), nextFireTime);
    }

    /**
     * Xóa Job
     */
    public void delete(BaseSchedulableEntity entity) {
        JobKey jobKey = getJobKey(entity);
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
     * Xóa toàn bộ Job trong một Group
     */
    public void deleteAllInGroup(String groupName) {
        try {
            GroupMatcher<JobKey> matcher = GroupMatcher.groupEquals(groupName);
            var jobKeys = scheduler.getJobKeys(matcher);
            
            if (jobKeys != null && !jobKeys.isEmpty()) {
                scheduler.deleteJobs(new java.util.ArrayList<>(jobKeys));
                log.info("Deleted {} jobs in group '{}'", jobKeys.size(), groupName);
            }
        } catch (SchedulerException e) {
            log.error("Error deleting group '{}': {}", groupName, e.getMessage());
        }
    }

    /**
     * Tạm dừng Job
     */
    public void pause(BaseSchedulableEntity entity) {
        try {
            scheduler.pauseJob(getJobKey(entity));
            log.info("Paused job: {}", entity.getJobName());
        } catch (SchedulerException e) {
            throw new InternalServerErrorException("Failed to pause job");
        }
    }

    /**
     * Tiếp tục Job
     */
    public void resume(BaseSchedulableEntity entity) {
        try {
            scheduler.resumeJob(getJobKey(entity));
            log.info("Resumed job: {}", entity.getJobName());
        } catch (SchedulerException e) {
            throw new InternalServerErrorException("Failed to resume job");
        }
    }

    /**
     * Chạy ngay lập tức
     */
    public void triggerNow(BaseSchedulableEntity entity) {
        try {
            scheduler.triggerJob(getJobKey(entity), entity.getJobDataMap());
            log.info("Manually triggered job: {}", entity.getJobName());
        } catch (SchedulerException e) {
            log.error("Error triggering job '{}': {}", entity.getJobName(), e.getMessage());
            throw new InternalServerErrorException("Failed to trigger job manually");
        }
    }

    // --- Helpers ---

    private Trigger buildCronTrigger(BaseSchedulableEntity entity) {
        return TriggerBuilder.newTrigger()
                .withIdentity(getTriggerKey(entity))
                .withSchedule(CronScheduleBuilder.cronSchedule(entity.getCronExpression())
                        .inTimeZone(TimeZone.getTimeZone(appTimezone))
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();
    }

    private JobKey getJobKey(BaseSchedulableEntity entity) {
        return JobKey.jobKey(entity.getJobName(), entity.getJobGroup());
    }

    private TriggerKey getTriggerKey(BaseSchedulableEntity entity) {
        return TriggerKey.triggerKey("Trigger_" + entity.getJobName(), entity.getJobGroup());
    }
}