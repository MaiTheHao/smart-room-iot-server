package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dao.Co2SensorDao;
import com.iviet.ivshs.dao.Co2MetricDao;
import com.iviet.ivshs.dto.Co2MetricDto;
import com.iviet.ivshs.dto.SensorMetadataDto;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import com.iviet.ivshs.entities.Co2Metric;
import com.iviet.ivshs.entities.Co2SensorLan;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.MetricDomain;
import com.iviet.ivshs.shared.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.shared.util.LocalContextUtil;
import com.iviet.ivshs.service.Co2MetricService;
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
public class Co2MetricServiceImpl implements Co2MetricService {

    private final Co2SensorDao co2SensorDao;
    private final Co2MetricDao co2MetricDao;

    // ========== TelemetryCRUDServiceStrategy ==========

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.SENSOR_CO2;
    }

    @Override
    @Transactional
    public void create(TelemetryResponseDto.DeviceDto data) {
        JsonNode co2Node = data.getData().get("co2");
        if (co2Node == null)
            return;

        Double co2Value;
        if (co2Node.isNumber()) {
            co2Value = co2Node.asDouble();
        } else if (co2Node.isTextual()) {
            try {
                co2Value = Double.parseDouble(co2Node.asText());
            } catch (NumberFormatException e) {
                log.error("Failed to parse CO2 value '{}' for sensor {}: {}",
                    co2Node.asText(), data.getNaturalId(), e.getMessage());
                return;
            }
        } else {
            return;
        }

        var sensor = co2SensorDao.findByNaturalId(data.getNaturalId())
            .orElseThrow(() -> new NotFoundException("CO2 sensor not found with natural ID: " + data.getNaturalId()));

        sensor.setCurrentCO2(co2Value);
        co2SensorDao.save(sensor);

        Co2Metric metric = new Co2Metric();
        metric.setTargetCategory("SENSOR_CO2");
        metric.setTargetId(sensor.getId());
        metric.setTimestamp(Instant.now());
        metric.setCo2(co2Value);

        co2MetricDao.save(Collections.singletonList(metric));
        log.info("Successfully saved CO2 metric {}ppm for sensor {}", co2Value, sensor.getNaturalId());
    }

    // ========== MetricServiceStrategy ==========

    @Override
    public MetricDomain getSupportedDomain() {
        return MetricDomain.CO2;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getLatest(String category, Long targetId) {
        return co2MetricDao.findLatest(targetId)
                .map(Co2MetricDto::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(String category, Long targetId, Instant from, Instant to) {
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        int divisor = TelemetryTimeGroup.getDivisorForRange(limitedFrom, to);
        return co2MetricDao.findHistory(targetId, limitedFrom, to, divisor);
    }

    // ========== SensorTelemetryServiceStrategy ==========

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(Long sensorId, Instant from, Instant to) {
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        return co2MetricDao.findHistory(sensorId, limitedFrom, to)
                .stream()
                .map(Co2MetricDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistoryByNaturalId(String naturalId, Instant from, Instant to) {
        var sensor = co2SensorDao.findByNaturalId(naturalId)
            .orElseThrow(() -> new NotFoundException("CO2 sensor not found: " + naturalId));
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        return co2MetricDao.findHistory(sensor.getId(), limitedFrom, to)
                .stream()
                .map(Co2MetricDto::fromEntity)
                .toList();
    }

    // ========== SensorMetadataServiceStrategy ==========

    @Override
    @Transactional(readOnly = true)
    public List<SensorMetadataDto> getSensorByRoomId(Long roomId) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return co2SensorDao.findAllByRoomIdWithTranslations(roomId).stream()
                .map(entity -> {
                    Co2SensorLan lan = entity.getTranslations().stream()
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
        return co2SensorDao.findAllWithTranslations().stream()
                .map(entity -> {
                    Co2SensorLan lan = entity.getTranslations().stream()
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
        var entity = co2SensorDao.findById(id)
                .orElseThrow(() -> new NotFoundException("CO2 sensor not found with ID: " + id));
        Co2SensorLan lan = entity.getTranslations().stream()
                .filter(t -> t.getLangCode().equals(langCode))
                .findFirst()
                .orElseGet(() -> entity.getTranslations().stream().findFirst().orElse(null));
        return SensorMetadataDto.from(entity, lan);
    }

    @Override
    @Transactional(readOnly = true)
    public SensorMetadataDto getSensorByNaturalId(String naturalId) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        var entity = co2SensorDao.findByNaturalId(naturalId)
                .orElseThrow(() -> new NotFoundException("CO2 sensor not found with natural ID: " + naturalId));
        Co2SensorLan lan = entity.getTranslations().stream()
                .filter(t -> t.getLangCode().equals(langCode))
                .findFirst()
                .orElseGet(() -> entity.getTranslations().stream().findFirst().orElse(null));
        return SensorMetadataDto.from(entity, lan);
    }
}
