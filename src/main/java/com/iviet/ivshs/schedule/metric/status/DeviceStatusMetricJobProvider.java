package com.iviet.ivshs.schedule.metric.status;

import com.iviet.ivshs.schedule.metric.MetricJobProvider;
import com.iviet.ivshs.schedule.metric.MetricJobRegistration;
import lombok.RequiredArgsConstructor;
import com.iviet.ivshs.properties.AppProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeviceStatusMetricJobProvider implements MetricJobProvider {

    private final AppProperties appProperties;

    @Override
    public List<MetricJobRegistration> getMetricJobs() {
        return List.of(
            MetricJobRegistration.builder()
                .name(DeviceStatusMetricJob.JOB_NAME)
                .group(DeviceStatusMetricJob.JOB_GROUP)
                .jobClass(DeviceStatusMetricJob.class)
                .intervalSeconds(appProperties.getMetricStatusIntervalSeconds())
                .build()
        );
    }
}
