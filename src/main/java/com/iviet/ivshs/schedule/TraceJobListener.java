package com.iviet.ivshs.schedule;

import org.slf4j.MDC;
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
        MDC.put("traceId", traceId);
        MDC.put("scenarioId", "job:" + jobName);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        MDC.clear();
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        MDC.clear();
    }
}
