package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.TemperatureMetricDao;
import com.iviet.ivshs.dto.TemperatureMetricDto;
import com.iviet.ivshs.shared.enumeration.MetricDomain;
import com.iviet.ivshs.shared.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.service.strategy.MetricServiceStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemperatureMetricServiceImpl implements MetricServiceStrategy {

    private final TemperatureMetricDao temperatureMetricDao;

    @Override
    public MetricDomain getSupportedDomain() {
        return MetricDomain.TEMPERATURE;
    }

    @Override
    public Object getLatest(String category, Long targetId) {
        return temperatureMetricDao.findLatest(targetId)
                .map(TemperatureMetricDto::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(String category, Long targetId, Instant from, Instant to) {
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        int divisor = TelemetryTimeGroup.getDivisorForRange(limitedFrom, to);
        return temperatureMetricDao.findHistory(targetId, limitedFrom, to, divisor);
    }
}
