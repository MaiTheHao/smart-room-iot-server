package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.TemperatureMetricDao;
import com.iviet.ivshs.dto.RoomTemperatureMetricDto;
import com.iviet.ivshs.dto.TemperatureMetricDto;
import com.iviet.ivshs.shared.enumeration.MetricDomain;
import com.iviet.ivshs.shared.enumeration.SensorMetricCategory;
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
        if (SensorMetricCategory.fromString(category) == SensorMetricCategory.ROOM) {
            return temperatureMetricDao.findLatestByRoomId(targetId).orElse(null);
        }
        return temperatureMetricDao.findLatest(targetId)
                .map(TemperatureMetricDto::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(String category, Long targetId, Instant from, Instant to) {
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        int divisor = TelemetryTimeGroup.getDivisorForRange(limitedFrom, to);
        if (SensorMetricCategory.fromString(category) == SensorMetricCategory.ROOM) {
            return temperatureMetricDao.findHistoryByRoomId(targetId, limitedFrom, to, divisor);
        }
        return temperatureMetricDao.findHistory(targetId, limitedFrom, to, divisor);
    }
}
