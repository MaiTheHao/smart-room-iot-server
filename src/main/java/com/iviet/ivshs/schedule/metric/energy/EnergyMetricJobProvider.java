package com.iviet.ivshs.schedule.metric.energy;

import com.iviet.ivshs.schedule.metric.MetricJobProvider;
import com.iviet.ivshs.schedule.metric.MetricJobRegistration;
import lombok.RequiredArgsConstructor;
import com.iviet.ivshs.properties.AppProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EnergyMetricJobProvider implements MetricJobProvider {
    
    private final AppProperties appProperties;

    @Override
    public List<MetricJobRegistration> getMetricJobs() {
        return List.of(
            MetricJobRegistration.builder()
                .name(EnergyMetricTelemetryJob.JOB_NAME)
                .group(EnergyMetricTelemetryJob.JOB_GROUP)
                .jobClass(EnergyMetricTelemetryJob.class)
                .intervalSeconds(appProperties.getMetricEnergyIntervalSeconds())
                .build(),
            MetricJobRegistration.builder()
                .name(EnergyMetricResetJob.JOB_NAME)
                .group(EnergyMetricResetJob.JOB_GROUP)
                .jobClass(EnergyMetricResetJob.class)
                .cronExpression(appProperties.getMetricEnergyResetCron())
                .build()
        );
    }
}
