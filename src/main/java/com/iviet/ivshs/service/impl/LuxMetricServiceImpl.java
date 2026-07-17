package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dao.LuxSensorDao;
import com.iviet.ivshs.dao.LuxMetricDao;
import com.iviet.ivshs.dto.LuxMetricDto;
import com.iviet.ivshs.dto.RoomLuxMetricDto;
import com.iviet.ivshs.dto.SensorMetadataDto;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import com.iviet.ivshs.entities.LuxMetric;
import com.iviet.ivshs.entities.LuxSensor;
import com.iviet.ivshs.entities.LuxSensorLan;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.MetricDomain;
import com.iviet.ivshs.shared.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.shared.util.LocalContextUtil;
import com.iviet.ivshs.service.LuxMetricService;
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
public class LuxMetricServiceImpl implements LuxMetricService {

    private final LuxSensorDao luxSensorDao;
    private final LuxMetricDao luxMetricDao;

    // ========== TelemetryCRUDServiceStrategy ==========

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.SENSOR_LUX;
    }

    @Override
    @Transactional
    public void create(TelemetryResponseDto.DeviceDto data) {
        JsonNode luxNode = data.getData().get("lux");
        if (luxNode == null)
            return;

        Double luxValue;
        if (luxNode.isNumber()) {
            luxValue = luxNode.asDouble();
        } else if (luxNode.isTextual()) {
            try {
                luxValue = Double.parseDouble(luxNode.asText());
            } catch (NumberFormatException e) {
                log.error("Failed to parse lux value '{}' for sensor {}: {}",
                    luxNode.asText(), data.getNaturalId(), e.getMessage());
                return;
            }
        } else {
            return;
        }

        var sensor = luxSensorDao.findByNaturalId(data.getNaturalId())
            .orElseThrow(() -> new NotFoundException("Lux sensor not found with natural ID: " + data.getNaturalId()));

        sensor.setCurrentLux(luxValue);
        luxSensorDao.save(sensor);

        LuxMetric metric = new LuxMetric();
        metric.setTargetCategory("SENSOR_LUX");
        metric.setTargetId(sensor.getId());
        metric.setTimestamp(Instant.now());
        metric.setLux(luxValue);

        luxMetricDao.save(Collections.singletonList(metric));
        log.info("Successfully saved lux metric {}lx for sensor {}", luxValue, sensor.getNaturalId());
    }

    // ========== MetricServiceStrategy ==========

    @Override
    public MetricDomain getSupportedDomain() {
        return MetricDomain.LUX;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getLatest(String category, Long targetId) {
        if (SensorMetricCategory.fromString(category) == SensorMetricCategory.ROOM) {
            List<Double> values = luxMetricDao.findCurrentValuesByRoomId(targetId);
            return Calculator.median(values)
                    .map(median -> RoomLuxMetricDto.builder()
                            .timestamp(Instant.now())
                            .medianLux(median)
                            .build())
                    .orElse(null);
        }
        return luxMetricDao.findLatest(targetId)
                .map(LuxMetricDto::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(String category, Long targetId, Instant from, Instant to) {
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        int divisor = TelemetryTimeGroup.getDivisorForRange(limitedFrom, to);
        if (SensorMetricCategory.fromString(category) == SensorMetricCategory.ROOM) {
            List<Object[]> raw = luxMetricDao.findRawHistoryByRoomId(targetId, limitedFrom, to, divisor);
            return aggregateMedianByBucket(raw,
                    (bucket, median) -> RoomLuxMetricDto.builder()
                            .timestamp(Instant.ofEpochSecond(bucket))
                            .medianLux(median)
                            .build());
        }
        return luxMetricDao.findHistory(targetId, limitedFrom, to, divisor);
    }

    // ========== SensorTelemetryServiceStrategy ==========

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(Long sensorId, Instant from, Instant to) {
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        return luxMetricDao.findHistory(sensorId, limitedFrom, to)
                .stream()
                .map(LuxMetricDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistoryByNaturalId(String naturalId, Instant from, Instant to) {
        var sensor = luxSensorDao.findByNaturalId(naturalId)
            .orElseThrow(() -> new NotFoundException("Lux sensor not found: " + naturalId));
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        return luxMetricDao.findHistory(sensor.getId(), limitedFrom, to)
                .stream()
                .map(LuxMetricDto::fromEntity)
                .toList();
    }

    // ========== SensorMetadataServiceStrategy ==========

    @Override
    @Transactional(readOnly = true)
    public List<SensorMetadataDto> getSensorMetadataByRoomId(Long roomId) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return luxSensorDao.findAllByRoomIdWithTranslations(roomId).stream()
                .map(entity -> {
                    LuxSensorLan lan = entity.getTranslations().stream()
                            .filter(t -> t.getLangCode().equals(langCode))
                            .findFirst()
                            .orElseGet(() -> entity.getTranslations().stream().findFirst().orElse(null));
                    return SensorMetadataDto.from(entity, lan);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorMetadataDto> getAllSensorMetadata() {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return luxSensorDao.findAllWithTranslations().stream()
                .map(entity -> {
                    LuxSensorLan lan = entity.getTranslations().stream()
                            .filter(t -> t.getLangCode().equals(langCode))
                            .findFirst()
                            .orElseGet(() -> entity.getTranslations().stream().findFirst().orElse(null));
                    return SensorMetadataDto.from(entity, lan);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SensorMetadataDto getSensorMetadataById(Long id) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        var entity = luxSensorDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Lux sensor not found with ID: " + id));
        LuxSensorLan lan = entity.getTranslations().stream()
                .filter(t -> t.getLangCode().equals(langCode))
                .findFirst()
                .orElseGet(() -> entity.getTranslations().stream().findFirst().orElse(null));
        return SensorMetadataDto.from(entity, lan);
    }

    @Override
    @Transactional(readOnly = true)
    public SensorMetadataDto getSensorMetadataByNaturalId(String naturalId) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        var entity = luxSensorDao.findByNaturalId(naturalId)
                .orElseThrow(() -> new NotFoundException("Lux sensor not found with natural ID: " + naturalId));
        LuxSensorLan lan = entity.getTranslations().stream()
                .filter(t -> t.getLangCode().equals(langCode))
                .findFirst()
                .orElseGet(() -> entity.getTranslations().stream().findFirst().orElse(null));
        return SensorMetadataDto.from(entity, lan);
    }

    @Override
    public LuxSensor getSensorById(Long id) {
        return luxSensorDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Lux sensor not found with id: " + id));
    }

    @Override
    public LuxSensor getSensorByNaturalId(String naturalId) {
        return luxSensorDao.findByNaturalId(naturalId)
                .orElseThrow(() -> new NotFoundException("Lux sensor not found with naturalId: " + naturalId));
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
