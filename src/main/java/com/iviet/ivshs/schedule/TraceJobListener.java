package com.iviet.ivshs.schedule;

import org.slf4j.MDC;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.stereotype.Component;
import com.iviet.ivshs.component.TraceLogger;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class TraceJobListener implements JobListener {

    private final TraceLogger traceLogger;

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

        context.put("startTime", Instant.now());

        log.info(">>> [JOB] START | ID: {} | Job: {}", traceId, jobName);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        MDC.clear();
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        String traceId = MDC.get("traceId");
        String scenarioId = MDC.get("scenarioId");
        String jobName = context.getJobDetail().getKey().getName();
        String jobClass = context.getJobDetail().getJobClass().getName();

        Instant startTime = (Instant) context.get("startTime");
        long duration = startTime != null ? Duration.between(startTime, Instant.now()).toMillis() : 0;
        int status = (jobException == null) ? 200 : 500;

        log.info("<<< [JOB] END | ID: {} | Status: {} | Duration: {}ms | Job: {}",
                traceId, status, duration, jobName);

        traceLogger.logTrace(traceId, scenarioId, "JOB", "EXECUTE", "job://" + jobName,
                status, duration, jobClass, "localhost");

        MDC.clear();
    }
}
