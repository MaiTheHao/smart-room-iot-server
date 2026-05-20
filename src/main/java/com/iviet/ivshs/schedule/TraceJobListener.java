package com.iviet.ivshs.schedule;

import org.apache.logging.log4j.ThreadContext;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class TraceJobListener implements JobListener {

    @Override
    public String getName() {
        return "TraceJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        String traceId = UUID.randomUUID().toString();
        String jobName = context.getJobDetail().getKey().getName();
        ThreadContext.put("traceId", traceId);
        ThreadContext.put("scenarioId", "job:" + jobName);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        ThreadContext.clearAll();
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        ThreadContext.clearAll();
    }
}
