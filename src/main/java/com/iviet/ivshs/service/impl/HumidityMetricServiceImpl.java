package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dao.HumiditySensorDao;
import com.iviet.ivshs.dao.HumidityMetricDao;
import com.iviet.ivshs.dto.HumidityMetricDto;
import com.iviet.ivshs.dto.RoomHumidityMetricDto;
import com.iviet.ivshs.dto.SensorMetadataDto;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import com.iviet.ivshs.entities.HumidityMetric;
import com.iviet.ivshs.entities.HumiditySensorLan;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.MetricDomain;
import com.iviet.ivshs.shared.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.shared.util.LocalContextUtil;
import com.iviet.ivshs.service.HumidityMetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.iviet.ivshs.shared.enumeration.SensorMetricCategory;
import com.iviet.ivshs.shared.util.Calculator;

@Slf4j
@Service
@RequiredArgsConstructor
public class HumidityMetricServiceImpl implements HumidityMetricService {

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
        if (SensorMetricCategory.fromString(category) == SensorMetricCategory.ROOM) {
            List<Double> values = humidityMetricDao.findCurrentValuesByRoomId(targetId);
            return Calculator.median(values)
                    .map(median -> RoomHumidityMetricDto.builder()
                            .timestamp(Instant.now())
                            .medianHumidity(median)
                            .build())
                    .orElse(null);
        }
        return humidityMetricDao.findLatest(targetId)
                .map(HumidityMetricDto::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(String category, Long targetId, Instant from, Instant to) {
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        int divisor = TelemetryTimeGroup.getDivisorForRange(limitedFrom, to);
        if (SensorMetricCategory.fromString(category) == SensorMetricCategory.ROOM) {
            List<Object[]> raw = humidityMetricDao.findRawHistoryByRoomId(targetId, limitedFrom, to, divisor);
            return aggregateMedianByBucket(raw,
                    (bucket, median) -> RoomHumidityMetricDto.builder()
                            .timestamp(Instant.ofEpochSecond(bucket))
                            .medianHumidity(median)
                            .build());
        }
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

    // ========== SensorMetadataServiceStrategy ==========

    @Override
    @Transactional(readOnly = true)
    public List<SensorMetadataDto> getSensorByRoomId(Long roomId) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return humiditySensorDao.findAllByRoomIdWithTranslations(roomId).stream()
                .map(entity -> {
                    HumiditySensorLan lan = entity.getTranslations().stream()
                            .filter(t -> t.getLangCode().equals(langCode))
                            .findFirst()
                            .orElseGet(() -> entity.getTranslations().stream().findFirst().orElse(null));
                    return SensorMetadataDto.from(entity, lan);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorMetadataDto> getAllSensor() {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return humiditySensorDao.findAllWithTranslations().stream()
                .map(entity -> {
                    HumiditySensorLan lan = entity.getTranslations().stream()
                            .filter(t -> t.getLangCode().equals(langCode))
                            .findFirst()
                            .orElseGet(() -> entity.getTranslations().stream().findFirst().orElse(null));
                    return SensorMetadataDto.from(entity, lan);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SensorMetadataDto getSensorById(Long id) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        var entity = humiditySensorDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Humidity sensor not found with ID: " + id));
        HumiditySensorLan lan = entity.getTranslations().stream()
                .filter(t -> t.getLangCode().equals(langCode))
                .findFirst()
                .orElseGet(() -> entity.getTranslations().stream().findFirst().orElse(null));
        return SensorMetadataDto.from(entity, lan);
    }

    @Override
    @Transactional(readOnly = true)
    public SensorMetadataDto getSensorByNaturalId(String naturalId) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        var entity = humiditySensorDao.findByNaturalId(naturalId)
                .orElseThrow(() -> new NotFoundException("Humidity sensor not found with natural ID: " + naturalId));
        HumiditySensorLan lan = entity.getTranslations().stream()
                .filter(t -> t.getLangCode().equals(langCode))
                .findFirst()
                .orElseGet(() -> entity.getTranslations().stream().findFirst().orElse(null));
        return SensorMetadataDto.from(entity, lan);
    }

    /**
     * Nhóm raw (bucket, value) theo bucket → tính Median từng bucket → map sang DTO.
     */
    private <T> List<T> aggregateMedianByBucket(List<Object[]> raw,
            BiFunction<Long, Double, T> mapper) {
        // LinkedHashMap giữ thứ tự bucket đã sort từ query
        Map<Long, List<Double>> byBucket = new LinkedHashMap<>();
        for (Object[] row : raw) {
            long bucket = ((Number) row[0]).longValue();
            Double value = row[1] != null ? ((Number) row[1]).doubleValue() : null;
            byBucket.computeIfAbsent(bucket, k -> new ArrayList<>()).add(value);
        }
        return byBucket.entrySet().stream()
                .flatMap(entry -> Calculator.median(entry.getValue())
                        .map(median -> mapper.apply(entry.getKey(), median))
                        .stream())
                .toList();
    }
}
