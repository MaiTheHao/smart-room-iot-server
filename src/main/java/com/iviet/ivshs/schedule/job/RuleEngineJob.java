package com.iviet.ivshs.schedule.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.service.RuleService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@DisallowConcurrentExecution
public class RuleEngineJob implements Job {

    public static final String JOB_NAME = "GLOBAL_RULE_SCAN_JOB";
    public static final String JOB_GROUP = "RULE_ENGINE_SYSTEM";

    @Autowired
    private RuleService ruleService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            ruleService.executeGlobalRuleScan();
        } catch (Exception e) {
            log.error("Rule Engine Scan failed: {}", e.getMessage(), e);
        }
    }
}
