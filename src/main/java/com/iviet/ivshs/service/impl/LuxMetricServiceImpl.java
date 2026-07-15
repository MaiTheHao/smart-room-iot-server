package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dao.LuxSensorDao;
import com.iviet.ivshs.dao.LuxMetricDao;
import com.iviet.ivshs.dto.LuxMetricDto;
import com.iviet.ivshs.dto.SensorMetadataDto;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import com.iviet.ivshs.entities.LuxMetric;
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
import java.util.Collections;
import java.util.List;

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
        return luxMetricDao.findLatest(targetId)
                .map(LuxMetricDto::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(String category, Long targetId, Instant from, Instant to) {
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        int divisor = TelemetryTimeGroup.getDivisorForRange(limitedFrom, to);
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
    public List<SensorMetadataDto> getSensorByRoomId(Long roomId) {
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
    public List<SensorMetadataDto> getAllSensor() {
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
}
