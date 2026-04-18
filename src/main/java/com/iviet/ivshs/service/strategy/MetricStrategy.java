package com.iviet.ivshs.service.strategy;

import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.enumeration.MetricDomain;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import com.iviet.ivshs.schedule.metric.MetricJobRegistration;

public interface MetricStrategy {
    
    MetricDomain getSupportedDomain();

    Object getLatest(DeviceCategory category, Long targetId);

    List<?> getHistory(DeviceCategory category, Long targetId, Instant from, Instant to);

    default List<MetricJobRegistration> getMetricJobs() {
        return Collections.emptyList();
    }
}
