package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dao.EnergyMetricDao;
import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.dao.PowerConsumptionDao;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.ClientDto;
import com.iviet.ivshs.dto.EnergyMetricDto;
import com.iviet.ivshs.entities.EnergyMetric;
import com.iviet.ivshs.enumeration.EnergyMetricCategory;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.ClientService;
import com.iviet.ivshs.service.EnergyMetricService;
import com.iviet.ivshs.enumeration.MetricDomain;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.util.HttpClientUtil;
import com.iviet.ivshs.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

@Slf4j(topic = "ENERGY")
@Service
@RequiredArgsConstructor
public class EnergyMetricServiceImpl implements EnergyMetricService {

    private static final Duration RESET_RETRY_DELAY = Duration.ofSeconds(3);

    private final ClientService    clientService;
    private final EnergyMetricDao  energyMetricDao;
    private final LightDao         lightDao;
    private final FanDao           fanDao;
    private final AirConditionDao  airConditionDao;
    private final PowerConsumptionDao powerConsumptionDao;

    @Override
    @Transactional(readOnly = true)
    public List<EnergyMetricDto> getHistory(EnergyMetricCategory category, Long targetId, Instant from, Instant to) {
        from = TelemetryTimeGroup.limitRange(from, to);
        int divisor = TelemetryTimeGroup.getDivisorForRange(from, to);
        return energyMetricDao.findHistory(category, targetId, from, to, divisor);
    }

    @Override
    public MetricDomain getSupportedDomain() {
        return MetricDomain.ENERGY;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getLatest(DeviceCategory category, Long targetId) {
        return getLatest(mapToEnergyCategory(category), targetId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(DeviceCategory category, Long targetId, Instant from, Instant to) {
        return getHistory(mapToEnergyCategory(category), targetId, from, to);
    }

    private EnergyMetricCategory mapToEnergyCategory(DeviceCategory category) {
        if (category == DeviceCategory.POWER_CONSUMPTION) {
            return EnergyMetricCategory.ROOM;
        }
        try {
            return EnergyMetricCategory.valueOf(category.name());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Device category " + category.name() + " is not supported for energy metrics.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EnergyMetricDto> getLatest(EnergyMetricCategory category, Long targetId) {
        return energyMetricDao.findLatest(category, targetId);
    }

    @Override
    public void fetchFromGateways() {
        List<ClientDto> gateways = clientService.getAllGateways();
        if (gateways == null || gateways.isEmpty()) {
            log.warn("No gateways found, skipping energy telemetry fetch");
            return;
        }

        log.info("Fetch: Starting energy telemetry fetch for {} gateways", gateways.size());
        long start = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (ClientDto gateway : gateways) {
                executor.submit(() -> processGateway(gateway));
            }
        }

        log.info("Fetch: Completed in {}ms", System.currentTimeMillis() - start);
    }

    private void processGateway(ClientDto gateway) {
        log.debug("Fetch: Processing gateway [{}] ip={}", gateway.username(), gateway.ipAddress());
        Instant now = Instant.now();

        // LIGHT
        lightDao.findAllActiveByClientId(gateway.id()).forEach(d -> 
            fetchAndSave(UrlConstant.getLightEnergyTelemetryV1(gateway.ipAddress(), d.getNaturalId()), 
                d.getNaturalId(), EnergyMetricCategory.LIGHT, d.getId(), now));

        // FAN
        fanDao.findAllActiveByClientId(gateway.id()).forEach(d -> 
            fetchAndSave(UrlConstant.getFanEnergyTelemetryV1(gateway.ipAddress(), d.getNaturalId()), 
                d.getNaturalId(), EnergyMetricCategory.FAN, d.getId(), now));

        // AIR CONDITION
        airConditionDao.findAllActiveByClientId(gateway.id()).forEach(d -> 
            fetchAndSave(UrlConstant.getAcEnergyTelemetryV1(gateway.ipAddress(), d.getNaturalId()), 
                d.getNaturalId(), EnergyMetricCategory.AIR_CONDITION, d.getId(), now));

        // ROOM (Power Consumption)
        powerConsumptionDao.findAllActiveByClientId(gateway.id()).forEach(d -> 
            fetchAndSave(UrlConstant.getRoomEnergyTelemetryV1(gateway.ipAddress(), d.getNaturalId()), 
                d.getNaturalId(), EnergyMetricCategory.ROOM, d.getId(), now));
    }


    private void fetchAndSave(
        String url, String naturalId,
        EnergyMetricCategory category, Long targetId, Instant timestamp
    ) {
        try {
            HttpClientUtil.Response response = HttpClientUtil.get(url);

            if (!response.isSuccess()) {
                log.warn("Fetch: Non-2xx from RSPI for {}/{} (url={}): status={}", 
                    category.name(), naturalId, url, response.getStatusCode());
                return;
            }

            ApiResponse<EnergyMetricDto> apiResponse = JsonUtil.getMapper().readValue(
                response.getBody(),
                JsonUtil.getMapper().getTypeFactory()
                    .constructParametricType(ApiResponse.class, EnergyMetricDto.class)
            );

            if (apiResponse == null || apiResponse.getData() == null) {
                log.warn("Fetch: Empty data in RSPI response for {}/{}", category.name(), naturalId);
                return;
            }

            EnergyMetricDto dto = apiResponse.getData();
            EnergyMetric metric = dto.toEntity(naturalId, targetId);
            
            energyMetricDao.save(metric);
            log.debug("Save: Saved EnergyMetric category={} targetId={} power={}W",
                category.name(), targetId, dto.getPower());
        } catch (Exception e) {
            log.error("Fetch: Failed to parse/save metric for {}/{}: {}", 
                category.name(), naturalId, e.getMessage());
        }
    }

    @Override
    public void resetGateways() {
        List<ClientDto> gateways = clientService.getAllGateways();
        if (gateways == null || gateways.isEmpty()) {
            log.warn("Reset: No gateways found, skipping reset");
            return;
        }

        log.info("Reset: Starting daily energy reset for {} gateways", gateways.size());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (ClientDto gateway : gateways) {
                executor.submit(() -> processResetForGateway(gateway));
            }
        }

        log.info("Reset: Completed all device resets across all gateways");
    }

    private void processResetForGateway(ClientDto gateway) {
        log.debug("Reset: Processing gateway [{}] ip={}", gateway.username(), gateway.ipAddress());

        // Reset ACs
        airConditionDao.findAllActiveByClientId(gateway.id()).forEach(ac -> 
            callResetWithRetry(gateway.ipAddress(), ac.getNaturalId(), UrlConstant.getAcEnergyResetV1(gateway.ipAddress(), ac.getNaturalId()))
        );

        // Reset Fans
        fanDao.findAllActiveByClientId(gateway.id()).forEach(fan -> 
            callResetWithRetry(gateway.ipAddress(), fan.getNaturalId(), UrlConstant.getFanEnergyResetV1(gateway.ipAddress(), fan.getNaturalId()))
        );

        // Reset Lights
        lightDao.findAllActiveByClientId(gateway.id()).forEach(light -> 
            callResetWithRetry(gateway.ipAddress(), light.getNaturalId(), UrlConstant.getLightEnergyResetV1(gateway.ipAddress(), light.getNaturalId()))
        );

        // Reset Power Consumptions (ROOM)
        powerConsumptionDao.findAllActiveByClientId(gateway.id()).forEach(pc -> 
            callResetWithRetry(gateway.ipAddress(), pc.getNaturalId(), UrlConstant.getRoomEnergyResetV1(gateway.ipAddress(), pc.getNaturalId()))
        );
    }

    private void callResetWithRetry(String ip, String naturalId, String url) {
        HttpClientUtil.Response response = HttpClientUtil.post(url, "");
        if (response.isSuccess()) {
            log.info("Reset: Device [{}] on [{}] reset OK", naturalId, ip);
            return;
        }
        
        log.warn("Reset: Device [{}] on [{}] failed (status={}), retrying once...",
            naturalId, ip, response.getStatusCode());

        try {
            Thread.sleep(RESET_RETRY_DELAY.toMillis());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        HttpClientUtil.Response retry = HttpClientUtil.post(url, "");
        if (retry.isSuccess()) {
            log.info("Reset: Device [{}] on [{}] reset OK on retry", naturalId, ip);
        } else {
            log.error("Reset: Device [{}] on [{}] FAILED after retry (status={})",
                naturalId, ip, retry.getStatusCode());
        }
    }
}
