package com.iviet.ivshs.schedule.metric.status;

import com.iviet.ivshs.schedule.metric.MetricJobProvider;
import com.iviet.ivshs.schedule.metric.MetricJobRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeviceStatusMetricJobProvider implements MetricJobProvider {

    private final Environment env;

    @Override
    public List<MetricJobRegistration> getMetricJobs() {
        return List.of(
            MetricJobRegistration.builder()
                .name(DeviceStatusMetricJob.JOB_NAME)
                .group(DeviceStatusMetricJob.JOB_GROUP)
                .jobClass(DeviceStatusMetricJob.class)
                .intervalSeconds(env.getProperty("app.engine.metric_status.intervalSeconds", Integer.class, 300))
                .build()
        );
    }
}
