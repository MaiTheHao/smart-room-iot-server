package com.iviet.ivshs.scheduler.dynamic.base;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GenericSchedulableJob implements Job {

    @Autowired
    private JobProcessorFactory factory;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long jobId = context.getJobDetail().getJobDataMap().getLong("id");
        String typeStr = context.getJobDetail().getJobDataMap().getString("type");
        JobProcessorType type = JobProcessorType.valueOf(typeStr);

        long start = System.currentTimeMillis();
        log.info("Starting execution for {} Job ID: {}", type, jobId);

        try {
            factory.getProcessor(type).processJob(jobId);
            log.info("Finished {} Job ID: {} in {}ms", type, jobId, System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Execution failed for {} Job ID {}: {}", type, jobId, e.getMessage());
            throw new JobExecutionException(e, false);
        }
    }
}
