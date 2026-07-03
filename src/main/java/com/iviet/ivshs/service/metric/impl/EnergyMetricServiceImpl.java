package com.iviet.ivshs.service.metric.impl;

import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dao.EnergyMetricDao;
import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.dao.PowerConsumptionDao;
import com.iviet.ivshs.dto.client.ClientDto;
import com.iviet.ivshs.dto.metric.EnergyMetricDto;
import com.iviet.ivshs.shared.util.MdcTaskWrapper;
import com.iviet.ivshs.entities.EnergyMetric;
import com.iviet.ivshs.integration.gateway.GatewayAdapter;
import com.iviet.ivshs.integration.gateway.GatewayAdapterRegistry;
import com.iviet.ivshs.integration.gateway.GatewayCommand;
import com.iviet.ivshs.integration.gateway.GatewayFetchResult;
import com.iviet.ivshs.integration.gateway.GatewayOperationResult;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.EnergyMetricCategory;
import com.iviet.ivshs.shared.enumeration.MetricDomain;
import com.iviet.ivshs.shared.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.service.client.ClientService;
import com.iviet.ivshs.service.metric.EnergyMetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnergyMetricServiceImpl implements EnergyMetricService {

    private static final java.time.Duration RESET_RETRY_DELAY = java.time.Duration.ofSeconds(3);

    private final ClientService clientService;
    private final EnergyMetricDao energyMetricDao;
    private final LightDao lightDao;
    private final FanDao fanDao;
    private final AirConditionDao airConditionDao;
    private final PowerConsumptionDao powerConsumptionDao;
    private final GatewayAdapterRegistry gatewayAdapterRegistry;

    @Override
    @Transactional(readOnly = true)
    public List<EnergyMetricDto> getHistory(EnergyMetricCategory category, Long targetId, Instant from, Instant to) {
        int divisor = TelemetryTimeGroup.getDivisorForRange(from, to);
        log.debug("Fetching energy history: category={}, targetId={}, from={}, to={}, divisor={}", category.name(), targetId, from, to, divisor);
        return energyMetricDao.findHistory(category, targetId, from, to, divisor);
    }

    @Override
    public MetricDomain getSupportedDomain() {
        return MetricDomain.ENERGY;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getLatest(String category, Long targetId) {
        return getLatest(EnergyMetricCategory.fromString(category), targetId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(String category, Long targetId, Instant from, Instant to) {
        return getHistory(EnergyMetricCategory.fromString(category), targetId, from, to);
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

        log.info("Starting energy telemetry fetch: gatewayCount={}", gateways.size());
        long start = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (ClientDto gateway : gateways) {
                executor.submit(MdcTaskWrapper.wrap(() -> {
                    try {
                        processGateway(gateway);
                    } catch (Exception e) {
                        log.error("Failed to process energy metrics: gateway={}", gateway.username(), e);
                    }
                }));
            }
        }

        log.info("Energy telemetry fetch completed: duration={}ms", System.currentTimeMillis() - start);
    }

    private void processGateway(ClientDto gateway) {
        GatewayAdapter adapter = gatewayAdapterRegistry.get(gateway.clientType());
        String ip = gateway.ipAddress();
        List<EnergyMetric> metricsToSave = new ArrayList<>();

        lightDao.findAllActiveByClientId(gateway.id()).forEach(d ->
            fetchAndCollect(adapter, ip,
                GatewayCommand.forDevice(d.getNaturalId(), DeviceCategory.LIGHT)
                    .specificType(d.getSpecificType())
                    .targetId(d.getId())
                    .metricCategory(EnergyMetricCategory.LIGHT.name())
                    .build(),
                metricsToSave));

        fanDao.findAllActiveByClientId(gateway.id()).forEach(d ->
            fetchAndCollect(adapter, ip,
                GatewayCommand.forDevice(d.getNaturalId(), DeviceCategory.FAN)
                    .specificType(d.getSpecificType())
                    .targetId(d.getId())
                    .metricCategory(EnergyMetricCategory.FAN.name())
                    .build(),
                metricsToSave));

        airConditionDao.findAllActiveByClientId(gateway.id()).forEach(d ->
            fetchAndCollect(adapter, ip,
                GatewayCommand.forDevice(d.getNaturalId(), DeviceCategory.AIR_CONDITION)
                    .specificType(d.getSpecificType())
                    .targetId(d.getId())
                    .metricCategory(EnergyMetricCategory.AIR_CONDITION.name())
                    .gatewayPath("ac")
                    .build(),
                metricsToSave));

        powerConsumptionDao.findAllActiveByClientId(gateway.id()).forEach(d ->
            fetchAndCollect(adapter, ip,
                GatewayCommand.forDevice(d.getNaturalId(), DeviceCategory.POWER_CONSUMPTION)
                    .targetId(d.getId())
                    .metricCategory(EnergyMetricCategory.ROOM.name())
                    .gatewayPath("power-consumption")
                    .build(),
                metricsToSave));

        if (!metricsToSave.isEmpty()) {
            energyMetricDao.save(metricsToSave);
            log.info("Saved energy metrics: count={}, gateway={}", metricsToSave.size(), gateway.username());
        } else {
            log.warn("No energy metrics collected: gateway={}", gateway.username());
        }
    }

    private void fetchAndCollect(GatewayAdapter adapter, String ip,
                                  GatewayCommand cmd, List<EnergyMetric> collector) {
        GatewayFetchResult<EnergyMetricDto> result = adapter.fetchEnergyMetric(ip, cmd);
        if (!result.success()) {
            log.debug("Energy metric fetch skipped [{}]: {}", cmd.naturalId(), result.message());
            return;
        }
        result.getData().ifPresent(dto -> {
            Long targetId = cmd.metaTargetId();
            String metricCategory = cmd.metaMetricCategory();
            if (targetId == null || metricCategory == null) {
                log.warn("GatewayCommand missing metadata for naturalId={}", cmd.naturalId());
                return;
            }
            dto.setTimestamp(Instant.now());
            collector.add(dto.toEntity(metricCategory, targetId));
        });
    }

    @Override
    public void resetGateways() {
        List<ClientDto> gateways = clientService.getAllGateways();
        if (gateways == null || gateways.isEmpty()) {
            log.warn("No gateways found, skipping daily energy reset");
            return;
        }

        log.info("Starting daily energy reset: gatewayCount={}", gateways.size());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (ClientDto gateway : gateways) {
                executor.submit(MdcTaskWrapper.wrap(() -> processResetForGateway(gateway)));
            }
        }

        log.info("Completed all device resets across all gateways");
    }

    private void processResetForGateway(ClientDto gateway) {
        GatewayAdapter adapter = gatewayAdapterRegistry.get(gateway.clientType());
        String ip = gateway.ipAddress();

        airConditionDao.findAllActiveByClientId(gateway.id()).forEach(ac ->
            callResetWithRetry(() -> adapter.resetEnergy(ip,
                GatewayCommand.forDevice(ac.getNaturalId(), DeviceCategory.AIR_CONDITION)
                    .specificType(ac.getSpecificType())
                    .duration(ac.getDuration())
                    .gatewayPath("ac")
                    .build()), ip, ac.getNaturalId()));

        fanDao.findAllActiveByClientId(gateway.id()).forEach(fan ->
            callResetWithRetry(() -> adapter.resetEnergy(ip,
                GatewayCommand.forDevice(fan.getNaturalId(), DeviceCategory.FAN)
                    .specificType(fan.getSpecificType())
                    .duration(fan.getDuration())
                    .gatewayPath("fan")
                    .build()), ip, fan.getNaturalId()));

        lightDao.findAllActiveByClientId(gateway.id()).forEach(light ->
            callResetWithRetry(() -> adapter.resetEnergy(ip,
                GatewayCommand.forDevice(light.getNaturalId(), DeviceCategory.LIGHT)
                    .specificType(light.getSpecificType())
                    .gatewayPath("light")
                    .build()), ip, light.getNaturalId()));

        powerConsumptionDao.findAllActiveByClientId(gateway.id()).forEach(pc ->
            callResetWithRetry(() -> adapter.resetEnergy(ip,
                GatewayCommand.forDevice(pc.getNaturalId(), DeviceCategory.POWER_CONSUMPTION)
                    .gatewayPath("power-consumption")
                    .build()), ip, pc.getNaturalId()));
    }

    private void callResetWithRetry(java.util.function.Supplier<GatewayOperationResult> resetCall, String ip, String naturalId) {
        GatewayOperationResult result = resetCall.get();
        if (result.success()) {
            log.info("Device energy reset succeeded: naturalId={}, ip={}", naturalId, ip);
            return;
        }

        log.warn("Device energy reset failed, retrying: naturalId={}, ip={}, message={}", naturalId, ip, result.message());

        try {
            Thread.sleep(RESET_RETRY_DELAY.toMillis());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        GatewayOperationResult retry = resetCall.get();
        if (retry.success()) {
            log.info("Device energy reset succeeded on retry: naturalId={}, ip={}", naturalId, ip);
        } else {
            log.error("Device energy reset failed after retry: naturalId={}, ip={}, message={}", naturalId, ip, retry.message());
        }
    }
}
