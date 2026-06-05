package com.iviet.ivshs.scheduler.metric.energy;

import com.iviet.ivshs.core.properties.EngineProperties;
import com.iviet.ivshs.scheduler.metric.MetricJobProvider;
import com.iviet.ivshs.scheduler.metric.MetricJobRegistration;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EnergyMetricJobProvider implements MetricJobProvider {

        private final EngineProperties engineProperties;

        @Override
        public List<MetricJobRegistration> getMetricJobs() {
                return List.of(MetricJobRegistration.builder().name(EnergyMetricTelemetryJob.JOB_NAME).group(EnergyMetricTelemetryJob.JOB_GROUP).jobClass(EnergyMetricTelemetryJob.class)
                                .intervalSeconds(engineProperties.getMetricEnergyIntervalSeconds()).build(),
                                MetricJobRegistration.builder().name(EnergyMetricResetJob.JOB_NAME).group(EnergyMetricResetJob.JOB_GROUP).jobClass(EnergyMetricResetJob.class).cronExpression(engineProperties.getMetricEnergyResetCron()).build());
        }
}
