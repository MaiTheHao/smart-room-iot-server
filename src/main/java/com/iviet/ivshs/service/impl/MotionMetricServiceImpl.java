package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dao.MotionDetectorDao;
import com.iviet.ivshs.dao.MotionMetricDao;
import com.iviet.ivshs.dto.SensorEventRequestDto;
import com.iviet.ivshs.dto.SensorMetadataDto;
import com.iviet.ivshs.entities.MotionDetector;
import com.iviet.ivshs.entities.MotionDetectorLan;
import com.iviet.ivshs.entities.MotionMetric;
import com.iviet.ivshs.event.RoomMotionDetectedEvent;
import com.iviet.ivshs.service.MotionMetricService;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import com.iviet.ivshs.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MotionMetricServiceImpl implements MotionMetricService {

    private static final String FIELD_MOTION_DETECTED = "motion_detected";

    private final MotionDetectorDao motionDetectorDao;
    private final MotionMetricDao motionMetricDao;
    private final ApplicationEventPublisher eventPublisher;

    // ========== EventTelemetryStrategy ==========

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.MOTION_DETECTOR;
    }

    @Override
    @Transactional
    public void processData(String naturalId, SensorEventRequestDto request) {
        if (request == null || request.getData() == null) {
            throw new BadRequestException("Event request and data cannot be null");
        }

        JsonNode data = request.getData();
        JsonNode motionNode = data.get(FIELD_MOTION_DETECTED);
        if (motionNode == null || !motionNode.isBoolean()) {
            throw new BadRequestException("Field '" + FIELD_MOTION_DETECTED + "' must be a boolean");
        }

        boolean motionDetected = motionNode.asBoolean();
        MotionDetector sensor = motionDetectorDao.findByNaturalId(naturalId)
                .orElseThrow(() -> new NotFoundException("Motion detector not found with naturalId: " + naturalId));

        Instant now = Instant.now();

        sensor.setCurrentMotion(motionDetected);
        sensor.setLastEventAt(now);
        motionDetectorDao.save(sensor);

        MotionMetric metric = new MotionMetric();
        metric.setTargetCategory(DeviceCategory.MOTION_DETECTOR.name());
        metric.setTargetId(sensor.getId());
        metric.setTimestamp(now);
        metric.setUnixMinute(now.getEpochSecond() / 60);
        metric.setMotionDetected(motionDetected);
        motionMetricDao.save(Collections.singletonList(metric));
        log.info("Saved MotionMetric (motion={}) for sensor {}", motionDetected, naturalId);

        if (motionDetected && sensor.getRoom() != null) {
            Long roomId = sensor.getRoom().getId();
            log.info("Publishing RoomMotionDetectedEvent for roomId={}", roomId);
            eventPublisher.publishEvent(new RoomMotionDetectedEvent(this, naturalId, roomId, data, now));
        }
    }

    // ========== SensorMetadataServiceStrategy ==========

    @Override
    @Transactional(readOnly = true)
    public List<SensorMetadataDto> getSensorMetadataByRoomId(Long roomId) {
        String lang = LocaleContextHolder.getLocale().getLanguage();
        return motionDetectorDao.findAllByRoomIdWithTranslations(roomId).stream()
                .map(entity -> SensorMetadataDto.from(entity, resolveTranslation(entity, lang)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorMetadataDto> getAllSensorMetadata() {
        String lang = LocaleContextHolder.getLocale().getLanguage();
        return motionDetectorDao.findAllWithTranslations().stream()
                .map(entity -> SensorMetadataDto.from(entity, resolveTranslation(entity, lang)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SensorMetadataDto getSensorMetadataById(Long id) {
        String lang = LocaleContextHolder.getLocale().getLanguage();
        MotionDetector entity = getSensorById(id);
        return SensorMetadataDto.from(entity, resolveTranslation(entity, lang));
    }

    @Override
    @Transactional(readOnly = true)
    public SensorMetadataDto getSensorMetadataByNaturalId(String naturalId) {
        String lang = LocaleContextHolder.getLocale().getLanguage();
        MotionDetector entity = getSensorByNaturalId(naturalId);
        return SensorMetadataDto.from(entity, resolveTranslation(entity, lang));
    }

    @Override
    @Transactional(readOnly = true)
    public MotionDetector getSensorById(Long id) {
        return motionDetectorDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Motion detector not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public MotionDetector getSensorByNaturalId(String naturalId) {
        return motionDetectorDao.findByNaturalId(naturalId)
                .orElseThrow(() -> new NotFoundException("Motion detector not found with naturalId: " + naturalId));
    }

    private MotionDetectorLan resolveTranslation(MotionDetector entity, String lang) {
        return entity.getTranslations().stream()
                .filter(t -> t.getLangCode().equalsIgnoreCase(lang))
                .findFirst()
                .or(() -> entity.getTranslations().stream().findFirst())
                .orElseGet(MotionDetectorLan::new);
    }
}
