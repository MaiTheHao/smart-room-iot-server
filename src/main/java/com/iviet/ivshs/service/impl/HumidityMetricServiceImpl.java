package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dao.HumiditySensorDao;
import com.iviet.ivshs.dao.HumidityMetricDao;
import com.iviet.ivshs.dto.HumidityMetricDto;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import com.iviet.ivshs.entities.HumidityMetric;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.MetricDomain;
import com.iviet.ivshs.shared.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.service.strategy.MetricServiceStrategy;
import com.iviet.ivshs.service.strategy.SensorTelemetryServiceStrategy;
import com.iviet.ivshs.service.strategy.TelemetryCRUDServiceStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HumidityMetricServiceImpl implements TelemetryCRUDServiceStrategy, MetricServiceStrategy, SensorTelemetryServiceStrategy {

    private final HumiditySensorDao humiditySensorDao;
    private final HumidityMetricDao humidityMetricDao;

    // ========== TelemetryCRUDServiceStrategy ==========

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.HUMIDITY;
    }

    @Override
    @Transactional
    public void create(TelemetryResponseDto.DeviceDto data) {
        JsonNode humidityNode = data.getData().get("humidity");
        if (humidityNode == null)
            return;

        Double humidityValue;
        if (humidityNode.isNumber()) {
            humidityValue = humidityNode.asDouble();
        } else if (humidityNode.isTextual()) {
            try {
                humidityValue = Double.parseDouble(humidityNode.asText());
            } catch (NumberFormatException e) {
                log.error("Failed to parse humidity value '{}' for sensor {}: {}", 
                    humidityNode.asText(), data.getNaturalId(), e.getMessage());
                return;
            }
        } else {
            return;
        }

        var sensor = humiditySensorDao.findByNaturalId(data.getNaturalId())
            .orElseThrow(() -> new NotFoundException("Humidity sensor not found with natural ID: " + data.getNaturalId()));

        sensor.setCurrentHumidity(humidityValue);
        humiditySensorDao.save(sensor);

        HumidityMetric metric = new HumidityMetric();
        metric.setTargetCategory("HUMIDITY");
        metric.setTargetId(sensor.getId());
        metric.setTimestamp(Instant.now());
        metric.setHumidity(humidityValue);

        humidityMetricDao.save(Collections.singletonList(metric));
        log.info("Successfully saved humidity metric {}% for sensor {}", humidityValue, sensor.getNaturalId());
    }

    // ========== MetricServiceStrategy ==========

    @Override
    public MetricDomain getSupportedDomain() {
        return MetricDomain.HUMIDITY;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getLatest(String category, Long targetId) {
        return humidityMetricDao.findLatest(targetId)
                .map(HumidityMetricDto::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(String category, Long targetId, Instant from, Instant to) {
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        int divisor = TelemetryTimeGroup.getDivisorForRange(limitedFrom, to);
        return humidityMetricDao.findHistory(targetId, limitedFrom, to, divisor);
    }

    // ========== SensorTelemetryServiceStrategy ==========

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(Long sensorId, Instant from, Instant to) {
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        return humidityMetricDao.findHistory(sensorId, limitedFrom, to)
                .stream()
                .map(HumidityMetricDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistoryByNaturalId(String naturalId, Instant from, Instant to) {
        var sensor = humiditySensorDao.findByNaturalId(naturalId)
            .orElseThrow(() -> new NotFoundException("Humidity sensor not found: " + naturalId));
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        return humidityMetricDao.findHistory(sensor.getId(), limitedFrom, to)
                .stream()
                .map(HumidityMetricDto::fromEntity)
                .toList();
    }
}
