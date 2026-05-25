package com.iviet.ivshs.schedule;

import org.slf4j.MDC;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.stereotype.Component;
import com.iviet.ivshs.apm.TraceLogger;
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
        Instant startTime = Instant.now();
        MDC.put("traceId", traceId);
        MDC.put("scenarioId", "job:" + jobName);
        MDC.put("startedAt", startTime.toString());

        context.put("startTime", startTime);

        log.info("Job execution started: name={}, traceId={}", jobName, traceId);
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

        String startedAtStr = MDC.get("startedAt");
        Instant startTime = startedAtStr != null ? Instant.parse(startedAtStr) : (Instant) context.get("startTime");
        Instant endedAt = Instant.now();
        long duration = startTime != null ? Duration.between(startTime, endedAt).toMillis() : 0;
        int status = (jobException == null) ? 200 : 500;

        if (jobException != null) {
            log.error("Job execution failed: name={}, traceId={}, duration={}ms", jobName, traceId, duration, jobException);
        } else {
            log.info("Job execution completed: name={}, traceId={}, duration={}ms", jobName, traceId, duration);
        }

        traceLogger.logTrace(TraceLogger.TraceData.builder()
                .traceId(traceId)
                .scenarioId(scenarioId)
                .type("JOB")
                .method("EXECUTE")
                .uri("job://" + jobName)
                .status(status)
                .controller(jobClass)
                .remote("localhost")
                .startedAt(startTime)
                .endedAt(endedAt)
                .build());

        MDC.clear();
    }
}
