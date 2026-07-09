package com.iviet.ivshs.service.metric;

import com.iviet.ivshs.shared.enumeration.EnergyMetricCategory;
import com.iviet.ivshs.dto.EnergyMetricDto;
import com.iviet.ivshs.service.telemetry.SensorTelemetryServiceStrategy;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EnergyMetricService extends MetricServiceStrategy, SensorTelemetryServiceStrategy {

    List<EnergyMetricDto> getHistory(EnergyMetricCategory category, Long targetId, Instant from, Instant to);

    Optional<EnergyMetricDto> getLatest(EnergyMetricCategory category, Long targetId);

    void fetchFromGateways();

    void resetGateways();
}
