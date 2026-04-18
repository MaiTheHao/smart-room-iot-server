package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dao.EnergyMetricDao;
import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.ClientDto;
import com.iviet.ivshs.dto.EnergyMetricDto;
import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.entities.BaseIoTEntity;
import com.iviet.ivshs.entities.EnergyMetric;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.enumeration.EnergyMetricCategory;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.ClientService;
import com.iviet.ivshs.service.EnergyMetricService;
import com.iviet.ivshs.enumeration.MetricDomain;
import com.iviet.ivshs.enumeration.DeviceCategory;
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
import java.util.function.Function;

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

    @Override
    @Transactional(readOnly = true)
    public List<EnergyMetricDto> getHistory(EnergyMetricCategory category, Long targetId, Instant from, Instant to) {
        int divisor = com.iviet.ivshs.enumeration.TelemetryTimeGroup.getDivisorForRange(from, to);
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

        fetchDevicesForCategory(gateway, EnergyMetricCategory.LIGHT,
            lightDao.findAllActiveByClientId(gateway.id()),
            light -> ((Light) light).getId(), now);

        fetchDevicesForCategory(gateway, EnergyMetricCategory.FAN,
            fanDao.findAllActiveByClientId(gateway.id()),
            fan -> ((Fan) fan).getId(), now);

        fetchDevicesForCategory(gateway, EnergyMetricCategory.AIR_CONDITION,
            airConditionDao.findAllActiveByClientId(gateway.id()),
            ac -> ((AirCondition) ac).getId(), now);
    }

    private <T extends BaseIoTEntity<?>> void fetchDevicesForCategory(
        ClientDto gateway,
        EnergyMetricCategory category,
        List<T> devices,
        Function<T, Long> targetIdExtractor,
        Instant timestamp
    ) {
        String deviceDomain = category.getDomain();
        for (T device : devices) {
            try {
                fetchAndSave(
                    gateway.ipAddress(),
                    deviceDomain,
                    device.getNaturalId(),
                    category,
                    targetIdExtractor.apply(device),
                    timestamp
                );
            } catch (Exception e) {
                log.error("Fetch: Failed {}/{} on gateway [{}]: {}",
                    deviceDomain, device.getNaturalId(), gateway.username(), e.getMessage());
            }
        }
    }

    private void fetchAndSave(
        String ip, String deviceDomain, String naturalId,
        EnergyMetricCategory category, Long targetId, Instant timestamp
    ) {
        String url = UrlConstant.getEnergyTelemetryV1(ip, deviceDomain, naturalId);
        HttpClientUtil.Response response = HttpClientUtil.get(url);

        if (!response.isSuccess()) {
            log.warn("Fetch: Non-2xx from RSPI for {}/{}: status={}", deviceDomain, naturalId, response.getStatusCode());
            return;
        }

        ApiResponse<EnergyMetricDto> apiResponse = JsonUtil.getMapper().convertValue(
            JsonUtil.parse(response.getBody()),
            JsonUtil.getMapper().getTypeFactory()
                .constructParametricType(ApiResponse.class, EnergyMetricDto.class)
        );

        if (apiResponse == null || apiResponse.getData() == null) {
            log.warn("Fetch: Empty data in RSPI response for {}/{}", deviceDomain, naturalId);
            return;
        }

        EnergyMetricDto dto = apiResponse.getData();

        EnergyMetric metric = EnergyMetric.builder()
            .category(category)
            .targetId(targetId)
            .timestamp(timestamp)
            .voltage(dto.getVoltage())
            .current(dto.getCurrent())
            .power(dto.getPower())
            .energy(dto.getEnergy())
            .frequency(dto.getFrequency())
            .powerFactor(dto.getPowerFactor())
            .build();

        energyMetricDao.save(metric);
        log.debug("Save: Saved EnergyMetric category={} targetId={} power={}W",
            category.name(), targetId, dto.getPower());
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
                executor.submit(() -> {
                    try {
                        callResetWithRetry(gateway);
                    } catch (Exception e) {
                        log.error("[ENERGY-RESET] Unexpected error for gateway [{}]: {}",
                            gateway.username(), e.getMessage(), e);
                    }
                });
            }
        }

        log.info("Reset: Completed all gateway resets");
    }

    private void callResetWithRetry(ClientDto gateway) {
        String url = UrlConstant.getEnergyResetV1(gateway.ipAddress());

        HttpClientUtil.Response response = HttpClientUtil.post(url, "");
        if (response.isSuccess()) {
            log.info("Reset: Gateway [{}] reset OK", gateway.username());
            return;
        }
        
        log.warn("Reset: Gateway [{}] failed (status={}), retrying once...",
            gateway.username(), response.getStatusCode());

        try {
            Thread.sleep(RESET_RETRY_DELAY.toMillis());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        HttpClientUtil.Response retry = HttpClientUtil.post(url, "");
        if (retry.isSuccess()) {
            log.info("Reset: Gateway [{}] reset OK on retry", gateway.username());
        } else {
            log.error("Reset: Gateway [{}] FAILED after retry (status={})",
                gateway.username(), retry.getStatusCode());
        }
    }
}
