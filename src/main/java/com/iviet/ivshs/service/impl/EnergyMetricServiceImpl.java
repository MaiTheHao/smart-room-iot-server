package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dao.EnergyMetricDao;
import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.dao.PowerConsumptionDao;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.ClientDto;
import com.iviet.ivshs.dto.EnergyMetricDto;
import com.iviet.ivshs.entities.EnergyMetric;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.enumeration.EnergyMetricCategory;
import com.iviet.ivshs.enumeration.MetricDomain;
import com.iviet.ivshs.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.ClientService;
import com.iviet.ivshs.service.EnergyMetricService;
import com.iviet.ivshs.service.client.gateway.GatewayMaintenanceClient;
import com.iviet.ivshs.service.client.gateway.GatewayTelemetryClient;
import com.iviet.ivshs.util.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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

    private final GatewayTelemetryClient   telemetryClient;
    private final GatewayMaintenanceClient maintenanceClient;

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
        String ip = gateway.ipAddress();
        List<EnergyMetric> metricsToSave = new ArrayList<>();

        // Fetch all LIGHT energy metrics and collect responses
        lightDao.findAllActiveByClientId(gateway.id()).forEach(d -> 
            filterResponse(
                telemetryClient.fetchLightEnergyMetric(ip, d.getNaturalId()),
                EnergyMetricCategory.LIGHT, d.getId(), d.getNaturalId(), metricsToSave
            )
        );

        // Fetch all FAN energy metrics and collect responses
        fanDao.findAllActiveByClientId(gateway.id()).forEach(d -> 
            filterResponse(
                telemetryClient.fetchFanEnergyMetric(ip, d.getNaturalId()),
                EnergyMetricCategory.FAN, d.getId(), d.getNaturalId(), metricsToSave
            )
        );

        // Fetch all AIR CONDITION energy metrics and collect responses
        airConditionDao.findAllActiveByClientId(gateway.id()).forEach(d -> 
            filterResponse(
                telemetryClient.fetchAcEnergyMetric(ip, d.getNaturalId()),
                EnergyMetricCategory.AIR_CONDITION, d.getId(), d.getNaturalId(), metricsToSave
            )
        );

        // Fetch all ROOM (Power Consumption) energy metrics and collect responses
        powerConsumptionDao.findAllActiveByClientId(gateway.id()).forEach(d -> 
            filterResponse(
                telemetryClient.fetchRoomEnergyMetric(ip, d.getNaturalId()),
                EnergyMetricCategory.ROOM, d.getId(), d.getNaturalId(), metricsToSave
            )
        );

        if (!metricsToSave.isEmpty()) {
            energyMetricDao.save(metricsToSave);
            log.info("Save: Saved {} energy metrics for gateway [{}]", metricsToSave.size(), gateway.username());
        } else {
            log.warn("Save: No energy metrics collected for gateway [{}]", gateway.username());
        }
    }


    private void filterResponse(
        ResponseEntity<ApiResponse<EnergyMetricDto>> response,
        EnergyMetricCategory category, Long targetId, String naturalId,
        List<EnergyMetric> metricsToSave
    ) {
        try {
            if (response == null) {
                log.warn("Fetch: Null response for {}/{}", category.name(), naturalId);
                return;
            }

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Fetch: Failed response for {}/{} (status={})", 
                    category.name(), naturalId, response.getStatusCode().value());
                return;
            }

            ApiResponse<EnergyMetricDto> apiResponse = response.getBody();
            if (apiResponse == null || apiResponse.getData() == null) {
                log.warn("Fetch: Empty body/data for {}/{}", category.name(), naturalId);
                return;
            }

            EnergyMetricDto dto = apiResponse.getData();
            EnergyMetric metric = dto.toEntity(naturalId, targetId);
            metricsToSave.add(metric);

            log.debug("Fetch: Collected metric for {}/{} power={}W", 
                category.name(), naturalId, dto.getPower());
        } catch (Exception e) {
            log.error("Fetch: Error processing metric for {}/{}: {}", 
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
        String ip = gateway.ipAddress();

        // Reset ACs
        airConditionDao.findAllActiveByClientId(gateway.id()).forEach(ac -> 
            callResetWithRetry(() -> maintenanceClient.resetAcEnergy(ip, ac.getNaturalId()), ip, ac.getNaturalId())
        );

        // Reset Fans
        fanDao.findAllActiveByClientId(gateway.id()).forEach(fan -> 
            callResetWithRetry(() -> maintenanceClient.resetFanEnergy(ip, fan.getNaturalId()), ip, fan.getNaturalId())
        );

        // Reset Lights
        lightDao.findAllActiveByClientId(gateway.id()).forEach(light -> 
            callResetWithRetry(() -> maintenanceClient.resetLightEnergy(ip, light.getNaturalId()), ip, light.getNaturalId())
        );

        // Reset Power Consumptions (ROOM)
        powerConsumptionDao.findAllActiveByClientId(gateway.id()).forEach(pc -> 
            callResetWithRetry(() -> maintenanceClient.resetRoomEnergy(ip, pc.getNaturalId()), ip, pc.getNaturalId())
        );
    }

    private void callResetWithRetry(java.util.function.Supplier<ResponseEntity<ApiResponse<String>>> resetCall, String ip, String naturalId) {
        ResponseEntity<ApiResponse<String>> response = resetCall.get();
        if (response != null && response.getStatusCode().is2xxSuccessful()) {
            log.info("Reset: Device [{}] on [{}] reset OK", naturalId, ip);
            return;
        }
        
        log.warn("Reset: Device [{}] on [{}] failed (status={}), retrying once...",
            naturalId, ip, response != null ? response.getStatusCode().value() : "NULL");

        try {
            Thread.sleep(RESET_RETRY_DELAY.toMillis());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        ResponseEntity<ApiResponse<String>> retry = resetCall.get();
        if (retry != null && retry.getStatusCode().is2xxSuccessful()) {
            log.info("Reset: Device [{}] on [{}] reset OK on retry", naturalId, ip);
        } else {
            log.error("Reset: Device [{}] on [{}] FAILED after retry (status={})",
                naturalId, ip, retry != null ? retry.getStatusCode().value() : "NULL");
        }
    }
}
