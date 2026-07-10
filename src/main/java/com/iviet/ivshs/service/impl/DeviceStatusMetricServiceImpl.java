package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dao.*;
import com.iviet.ivshs.dto.DeviceStatusMetricDto;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.entities.base.BaseIoTDevice;
import com.iviet.ivshs.service.DeviceStatusMetricService;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.MetricDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceStatusMetricServiceImpl implements DeviceStatusMetricService {

    private final LightDao lightDao;
    private final FanDao fanDao;
    private final AirConditionDao airConditionDao;
    private final DeviceStatusMetricDao deviceStatusMetricDao;
    private final ObjectMapper objectMapper;

    @Override
    public MetricDomain getSupportedDomain() {
        return MetricDomain.DEVICE_STATUS;
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceStatusMetricDto getLatest(String category, Long targetId) {
        return deviceStatusMetricDao.findLatest(category, targetId)
                .map(DeviceStatusMetricDto::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceStatusMetricDto> getHistory(String category, Long targetId, Instant from, Instant to) {
        return deviceStatusMetricDao.findHistory(category, targetId, from, to).stream()
                .map(DeviceStatusMetricDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void backupDeviceStatuses() {
        log.info("Starting local database device status backup");
        long start = System.currentTimeMillis();

        List<DeviceStatusMetric> latestMetrics = deviceStatusMetricDao.findAllLatestForEachDevice();
        java.util.Map<String, Long> latestVersionMap = new java.util.HashMap<>();
        for (DeviceStatusMetric m : latestMetrics) {
            latestVersionMap.put(m.getTargetCategory() + ":" + m.getTargetId(), m.getDeviceVersion());
        }

        List<DeviceStatusMetric> metricsToSave = new ArrayList<>();
        Instant now = Instant.now();

        processCategory(lightDao.findAllActive(),          DeviceCategory.LIGHT,            latestVersionMap, metricsToSave, now);
        processCategory(fanDao.findAllActive(),             DeviceCategory.FAN,             latestVersionMap, metricsToSave, now);
        processCategory(airConditionDao.findAllActive(),    DeviceCategory.AIR_CONDITION,   latestVersionMap, metricsToSave, now);

        if (!metricsToSave.isEmpty()) {
            deviceStatusMetricDao.save(metricsToSave);
            log.info("Successfully backed up {} device statuses (skipped duplicates) in {}ms", metricsToSave.size(), System.currentTimeMillis() - start);
        } else {
            log.info("All device statuses are unchanged. Skipped backup (duration {}ms)", System.currentTimeMillis() - start);
        }
    }

    private <T extends BaseIoTDevice<?>> void processCategory(
            List<T> activeEntities,
            DeviceCategory category,
            java.util.Map<String, Long> latestVersionMap,
            List<DeviceStatusMetric> metricsToSave,
            Instant now
    ) {
        for (T entity : activeEntities) {
            String key = category.name() + ":" + entity.getId();
            Long latestVersion = latestVersionMap.get(key);
            Long currentVersion = entity.getVersion() != null ? entity.getVersion() : 0L;

            if (latestVersion == null || !latestVersion.equals(currentVersion)) {
                Object businessData = entity.extractBusinessData();
                JsonNode statusData = DeviceStatusMetricDto.businessDataToJsonNode(businessData, objectMapper);
                metricsToSave.add(createMetricEntity(category, entity.getId(), now, statusData, currentVersion));
            }
        }
    }

    private DeviceStatusMetric createMetricEntity(DeviceCategory category, Long id, Instant timestamp, JsonNode data, Long version) {
        DeviceStatusMetric metric = new DeviceStatusMetric();
        metric.setTargetCategory(category.name());
        metric.setTargetId(id);
        metric.setTimestamp(timestamp);
        metric.setStatusData(data);
        metric.setDeviceVersion(version);
        return metric;
    }
}
