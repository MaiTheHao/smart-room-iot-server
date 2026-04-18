package com.iviet.ivshs.schedule.metric.energy;

import com.iviet.ivshs.schedule.metric.MetricJobProvider;
import com.iviet.ivshs.schedule.metric.MetricJobRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EnergyMetricJobProvider implements MetricJobProvider {
    
    private final Environment env;

    @Override
    public List<MetricJobRegistration> getMetricJobs() {
        return List.of(
            MetricJobRegistration.builder()
                .name(EnergyMetricTelemetryJob.JOB_NAME)
                .group(EnergyMetricTelemetryJob.JOB_GROUP)
                .jobClass(EnergyMetricTelemetryJob.class)
                .intervalSeconds(env.getProperty("app.engine.metric_energy.telemetry.intervalSeconds", Integer.class, 300))
                .build(),
            MetricJobRegistration.builder()
                .name(EnergyMetricResetJob.JOB_NAME)
                .group(EnergyMetricResetJob.JOB_GROUP)
                .jobClass(EnergyMetricResetJob.class)
                .cronExpression(env.getProperty("app.engine.metric_energy.reset.cron", String.class, "0 0 0 * * ?"))
                .build()
        );
    }
}
