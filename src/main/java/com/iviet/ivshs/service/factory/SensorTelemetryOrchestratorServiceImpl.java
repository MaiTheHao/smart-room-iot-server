package com.iviet.ivshs.service.factory;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.iviet.ivshs.service.factory.SensorTelemetryOrchestratorService;
import com.iviet.ivshs.service.strategy.SensorTelemetryServiceStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;

@Service
public class SensorTelemetryOrchestratorServiceImpl implements SensorTelemetryOrchestratorService {

    private final Map<DeviceCategory, SensorTelemetryServiceStrategy> strategies;

    public SensorTelemetryOrchestratorServiceImpl(List<SensorTelemetryServiceStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(SensorTelemetryServiceStrategy::getSupportedCategory, strategy -> strategy));
    }

    @Override
    public List<?> getHistory(Long sensorId, DeviceCategory category, Instant from, Instant to) {
        return getStrategy(category).getHistory(sensorId, from, to);
    }

    @Override
    public List<?> getHistoryByNaturalId(String naturalId, DeviceCategory category, Instant from, Instant to) {
        return getStrategy(category).getHistoryByNaturalId(naturalId, from, to);
    }

    private SensorTelemetryServiceStrategy getStrategy(DeviceCategory category) {
        if (category == null) {
            throw new BadRequestException("Category query parameter is required");
        }
        SensorTelemetryServiceStrategy strategy = strategies.get(category);
        if (strategy == null) {
            throw new BadRequestException("No sensor telemetry strategy found for category: " + category);
        }
        return strategy;
    }
}
