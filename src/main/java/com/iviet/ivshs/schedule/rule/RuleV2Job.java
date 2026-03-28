package com.iviet.ivshs.schedule.rule;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iviet.ivshs.service.RuleV2Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "RULEV2-JOB")
@Component
public class RuleV2Job implements Job {

  @Autowired
  private RuleV2Service ruleV2Service;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    Long ruleId = context.getJobDetail().getJobDataMap().getLong("id");
    long start = System.currentTimeMillis();

    log.info("Starting RuleV2 execution for ID: {}", ruleId);

    try {
      ruleV2Service.executeRuleLogic(ruleId);
      log.info("Finished RuleV2 execution for ID: {} in {}ms", ruleId, System.currentTimeMillis() - start);
    } catch (Exception e) {
      log.error("Execution failed for RuleV2 ID {}: {}", ruleId, e.getMessage());
      throw new JobExecutionException(e, false);
    }
  }
  
}
