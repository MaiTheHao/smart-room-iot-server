package com.iviet.ivshs.service.metric;

import com.iviet.ivshs.shared.enumeration.MetricDomain;

import java.time.Instant;
import java.util.List;

public interface MetricServiceStrategy {

    MetricDomain getSupportedDomain();

    Object getLatest(String category, Long targetId);

    List<?> getHistory(String category, Long targetId, Instant from, Instant to);
}
