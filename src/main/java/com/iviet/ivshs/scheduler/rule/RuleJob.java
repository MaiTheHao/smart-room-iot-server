package com.iviet.ivshs.scheduler.rule;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.service.rule.RuleService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RuleJob implements Job {

  @Autowired
  private RuleService ruleService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    Long ruleId = context.getJobDetail().getJobDataMap().getLong("id");
    long start = System.currentTimeMillis();

    log.info("Starting Rule execution for ID: {}", ruleId);

    try {
      ruleService.executeRuleLogic(ruleId);
      log.info("Finished Rule execution for ID: {} in {}ms", ruleId, System.currentTimeMillis() - start);
    } catch (Exception e) {
      log.error("Execution failed for Rule ID {}: {}", ruleId, e.getMessage());
      throw new JobExecutionException(e, false);
    }
  }

}
