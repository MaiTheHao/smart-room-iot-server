package com.iviet.ivshs.scheduler.system.metric.status;

import com.iviet.ivshs.core.properties.EngineProperties;
import com.iviet.ivshs.scheduler.system.metric.MetricJobProvider;
import com.iviet.ivshs.scheduler.system.metric.MetricJobRegistration;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeviceStatusMetricJobProvider implements MetricJobProvider {

    private final EngineProperties engineProperties;

    @Override
    public List<MetricJobRegistration> getMetricJobs() {
        return List.of(
                MetricJobRegistration.builder()
                        .name(DeviceStatusMetricJob.JOB_NAME)
                        .group(DeviceStatusMetricJob.JOB_GROUP)
                        .jobClass(DeviceStatusMetricJob.class)
                        .intervalSeconds(engineProperties.getMetricStatusIntervalSeconds())
                        .build());
    }
}
