package com.iviet.ivshs.service.strategy;


import com.iviet.ivshs.enumeration.MetricDomain;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import com.iviet.ivshs.schedule.metric.MetricJobRegistration;

public interface MetricServiceStrategy {
    
    MetricDomain getSupportedDomain();

    Object getLatest(String category, Long targetId);

    List<?> getHistory(String category, Long targetId, Instant from, Instant to);
}
