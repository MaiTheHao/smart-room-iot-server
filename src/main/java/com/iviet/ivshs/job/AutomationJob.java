package com.iviet.ivshs.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.service.AutomationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AutomationJob implements Job {
	
    @Autowired
    private AutomationService automationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long automationId = context.getJobDetail().getJobDataMap().getLong("id");
        
        log.info(">>> AutomationJob triggered for ID: {} on thread: {}", 
                automationId, Thread.currentThread().getName());
        
        try {
            automationService.executeAutomationLogic(automationId);
        } catch (Exception e) {
            log.error("❌ Error executing automation ID {}: {}", automationId, e.getMessage(), e);
            throw new JobExecutionException("Failed to execute automation: " + e.getMessage(), e);
        }
        
        log.info("✅ AutomationJob completed for ID: {}", automationId);
    }
}