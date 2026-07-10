package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iviet.ivshs.dao.*;
import com.iviet.ivshs.dto.DeviceStatusMetricDto;
import com.iviet.ivshs.entities.*;
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
    private final PowerConsumptionDao powerConsumptionDao;
    private final TemperatureDao temperatureDao;
    private final DeviceStatusMetricDao deviceStatusMetricDao;
    private final ObjectMapper objectMapper;

    @Override
    public MetricDomain getSupportedDomain() {
        return MetricDomain.DEVICE_STATUS;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getLatest(String category, Long targetId) {
        return deviceStatusMetricDao.findLatest(category, targetId)
                .map(dsm -> DeviceStatusMetricDto.builder()
                        .timestamp(dsm.getTimestamp())
                        .targetCategory(dsm.getTargetCategory())
                        .targetId(dsm.getTargetId())
                        .statusData(dsm.getStatusData())
                        .build())
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceStatusMetricDto> getHistory(String category, Long targetId, Instant from, Instant to) {
        return deviceStatusMetricDao.findHistory(category, targetId, from, to).stream()
                .map(dsm -> DeviceStatusMetricDto.builder()
                        .timestamp(dsm.getTimestamp())
                        .targetCategory(dsm.getTargetCategory())
                        .targetId(dsm.getTargetId())
                        .statusData(dsm.getStatusData())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void backupDeviceStatuses() {
        log.info("Starting local database device status backup");
        long start = System.currentTimeMillis();

        List<DeviceStatusMetric> latestMetrics = deviceStatusMetricDao.findAllLatestForEachDevice();
        java.util.Map<String, com.fasterxml.jackson.databind.JsonNode> latestStatusMap = new java.util.HashMap<>();
        for (DeviceStatusMetric m : latestMetrics) {
            if (m.getStatusData() != null) {
                latestStatusMap.put(m.getTargetCategory() + ":" + m.getTargetId(), m.getStatusData());
            }
        }

        List<DeviceStatusMetric> metricsToSave = new ArrayList<>();
        Instant now = Instant.now();

        // 1. Process active Lights
        lightDao.findAllActive().forEach(light -> {
            ObjectNode data = objectMapper.createObjectNode();
            if (light.getPower() != null) data.put("power", light.getPower().name());
            if (light.getLevel() != null) data.put("level", light.getLevel());

            String key = DeviceCategory.LIGHT.name() + ":" + light.getId();
            com.fasterxml.jackson.databind.JsonNode latest = latestStatusMap.get(key);
            if (latest == null || !latest.equals(data)) {
                metricsToSave.add(createMetricEntity(DeviceCategory.LIGHT, light.getId(), now, data));
            }
        });

        // 2. Process active Fans
        fanDao.findAllActive().forEach(fan -> {
            ObjectNode data = objectMapper.createObjectNode();
            if (fan.getPower() != null) data.put("power", fan.getPower().name());
            if (fan.getSpeed() != null) data.put("speed", fan.getSpeed());
            if (fan.getDuration() != null) data.put("duration", fan.getDuration());
            if (fan.getMode() != null) data.put("mode", fan.getMode().name());
            if (fan.getSwing() != null) data.put("swing", fan.getSwing().name());
            if (fan.getLight() != null) data.put("light", fan.getLight().name());

            String key = DeviceCategory.FAN.name() + ":" + fan.getId();
            com.fasterxml.jackson.databind.JsonNode latest = latestStatusMap.get(key);
            if (latest == null || !latest.equals(data)) {
                metricsToSave.add(createMetricEntity(DeviceCategory.FAN, fan.getId(), now, data));
            }
        });

        // 3. Process active ACs
        airConditionDao.findAllActive().forEach(ac -> {
            ObjectNode data = objectMapper.createObjectNode();
            if (ac.getPower() != null) data.put("power", ac.getPower().name());
            if (ac.getTemperature() != null) data.put("temperature", ac.getTemperature());
            if (ac.getMode() != null) data.put("mode", ac.getMode().name());
            if (ac.getFanSpeed() != null) data.put("fanSpeed", ac.getFanSpeed());
            if (ac.getSwing() != null) data.put("swing", ac.getSwing().name());
            if (ac.getDuration() != null) data.put("duration", ac.getDuration());

            String key = DeviceCategory.AIR_CONDITION.name() + ":" + ac.getId();
            com.fasterxml.jackson.databind.JsonNode latest = latestStatusMap.get(key);
            if (latest == null || !latest.equals(data)) {
                metricsToSave.add(createMetricEntity(DeviceCategory.AIR_CONDITION, ac.getId(), now, data));
            }
        });

        // 4. Process active Power Consumption sensors
        powerConsumptionDao.findAllActive().forEach(pc -> {
            ObjectNode data = objectMapper.createObjectNode();
            if (pc.getCurrentWatt() != null) data.put("currentWatt", pc.getCurrentWatt());

            String key = DeviceCategory.POWER_CONSUMPTION.name() + ":" + pc.getId();
            com.fasterxml.jackson.databind.JsonNode latest = latestStatusMap.get(key);
            if (latest == null || !latest.equals(data)) {
                metricsToSave.add(createMetricEntity(DeviceCategory.POWER_CONSUMPTION, pc.getId(), now, data));
            }
        });

        // 5. Process active Temperature sensors
        temperatureDao.findAllActive().forEach(temp -> {
            ObjectNode data = objectMapper.createObjectNode();
            if (temp.getCurrentValue() != null) data.put("currentValue", temp.getCurrentValue());

            String key = DeviceCategory.TEMPERATURE.name() + ":" + temp.getId();
            com.fasterxml.jackson.databind.JsonNode latest = latestStatusMap.get(key);
            if (latest == null || !latest.equals(data)) {
                metricsToSave.add(createMetricEntity(DeviceCategory.TEMPERATURE, temp.getId(), now, data));
            }
        });

        if (!metricsToSave.isEmpty()) {
            deviceStatusMetricDao.save(metricsToSave);
            log.info("Successfully backed up {} device statuses (skipped duplicates) in {}ms", metricsToSave.size(), System.currentTimeMillis() - start);
        } else {
            log.info("All device statuses are unchanged. Skipped backup (duration {}ms)", System.currentTimeMillis() - start);
        }
    }

    private DeviceStatusMetric createMetricEntity(DeviceCategory category, Long id, Instant timestamp, ObjectNode data) {
        DeviceStatusMetric metric = new DeviceStatusMetric();
        metric.setTargetCategory(category.name());
        metric.setTargetId(id);
        metric.setTimestamp(timestamp);
        metric.setStatusData(data);
        return metric;
    }
}
