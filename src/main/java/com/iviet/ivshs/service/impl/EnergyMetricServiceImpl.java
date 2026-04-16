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
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.ClientService;
import com.iviet.ivshs.service.EnergyMetricService;
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

@Slf4j
@Service
@RequiredArgsConstructor
// NO class-level @Transactional — transactions are managed at leaf level
public class EnergyMetricServiceImpl implements EnergyMetricService {

    private static final String CATEGORY_LIGHT = "LIGHT";
    private static final String CATEGORY_FAN   = "FAN";
    private static final String CATEGORY_AC    = "AC";

    /** RSPI device domains matching tasks.md Interface 4.1 spec */
    private static final String DOMAIN_LIGHT = "lights";
    private static final String DOMAIN_FAN   = "fans";
    private static final String DOMAIN_AC    = "air-conditions";

    private static final Duration RESET_RETRY_DELAY = Duration.ofSeconds(3);

    private final ClientService    clientService;
    private final EnergyMetricDao  energyMetricDao;
    private final LightDao         lightDao;
    private final FanDao           fanDao;
    private final AirConditionDao  airConditionDao;

    // ─────────────────────────────────────────────────────────────────────────
    // Client-facing Query  (short read-only transactions)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<EnergyMetricDto> getHistory(String category, Long targetId, Instant from, Instant to) {
        if (Duration.between(from, to).toDays() > 365) {
            throw new BadRequestException("Time range must not exceed 1 year");
        }
        return energyMetricDao.findHistory(category, targetId, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EnergyMetricDto> getNewest(String category, Long targetId) {
        return energyMetricDao.findNewest(category, targetId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // fetchFromGateways — called every 5 min by EnergyMetricTelemetryJob
    //
    // Architecture: NO @Transactional here.
    // Each Virtual Thread opens its own short-lived DB transactions only when
    // needed (find / save). I/O wait time never holds a DB connection open.
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void fetchFromGateways() {
        List<ClientDto> gateways = clientService.getAllGateways();
        if (gateways == null || gateways.isEmpty()) {
            log.warn("[ENERGY] No gateways found, skipping energy telemetry fetch");
            return;
        }

        log.info("[ENERGY] Starting energy telemetry fetch for {} gateways", gateways.size());
        long start = System.currentTimeMillis();

        // try-with-resources: executor.close() waits for all submitted tasks to complete
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (ClientDto gateway : gateways) {
                executor.submit(() -> processGateway(gateway));
            }
        } // ← blocks here until all virtual threads finish

        log.info("[ENERGY] Completed energy telemetry fetch in {}ms", System.currentTimeMillis() - start);
    }

    /**
     * Process a single gateway — no @Transactional.
     *
     * <p>DAO find calls each open their own short read transaction via the
     * EntityManager proxy (auto-commit read). The I/O phase (HTTP calls to RSPI)
     * does NOT hold any DB connection because there is no wrapping transaction.
     */
    private void processGateway(ClientDto gateway) {
        log.debug("[ENERGY] Processing gateway [{}] ip={}", gateway.username(), gateway.ipAddress());

        Instant now = Instant.now();

        // Each findAllActiveByClientId opens a short read transaction, fetches,
        // then CLOSES the connection before the HTTP phase begins.
        fetchDevicesForCategory(gateway, CATEGORY_LIGHT, DOMAIN_LIGHT,
            lightDao.findAllActiveByClientId(gateway.id()),
            light -> ((Light) light).getId(), now);

        fetchDevicesForCategory(gateway, CATEGORY_FAN, DOMAIN_FAN,
            fanDao.findAllActiveByClientId(gateway.id()),
            fan -> ((Fan) fan).getId(), now);

        fetchDevicesForCategory(gateway, CATEGORY_AC, DOMAIN_AC,
            airConditionDao.findAllActiveByClientId(gateway.id()),
            ac -> ((AirCondition) ac).getId(), now);
    }

    /**
     * Iterate all devices of one category, call RSPI, then save.
     * Any per-device failure is caught and logged; other devices continue.
     */
    private <T extends BaseIoTEntity<?>> void fetchDevicesForCategory(
        ClientDto gateway,
        String category,
        String deviceDomain,
        List<T> devices,
        Function<T, Long> targetIdExtractor,
        Instant timestamp
    ) {
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
                log.error("[ENERGY] Failed {}/{} on gateway [{}]: {}",
                    deviceDomain, device.getNaturalId(), gateway.username(), e.getMessage());
                // Best-effort: continue to next device
            }
        }
    }

    /**
     * HTTP fetch → parse → save.
     *
     * <p>The save step ({@code energyMetricDao.save()}) is the ONLY DB write here.
     * It carries its own {@code @Transactional} at the DAO level, so the transaction
     * opens immediately before the INSERT and commits immediately after — minimal
     * DB connection hold time.
     */
    private void fetchAndSave(
        String ip, String deviceDomain, String naturalId,
        String category, Long targetId, Instant timestamp
    ) {
        // ── HTTP I/O (no DB connection held) ──────────────────────────────────
        String url = UrlConstant.getEnergyTelemetryV1(ip, deviceDomain, naturalId);
        HttpClientUtil.Response response = HttpClientUtil.get(url);

        if (!response.isSuccess()) {
            log.warn("[ENERGY] Non-2xx from RSPI for {}/{}: status={}", deviceDomain, naturalId, response.getStatusCode());
            return;
        }

        // Parse ApiResponse<EnergyMetricDto>  using Jackson TypeFactory for generic type
        ApiResponse<EnergyMetricDto> apiResponse = JsonUtil.getMapper().convertValue(
            JsonUtil.parse(response.getBody()),
            JsonUtil.getMapper().getTypeFactory()
                .constructParametricType(ApiResponse.class, EnergyMetricDto.class)
        );

        if (apiResponse == null || apiResponse.getData() == null) {
            log.warn("[ENERGY] Empty data in RSPI response for {}/{}", deviceDomain, naturalId);
            return;
        }

        EnergyMetricDto dto = apiResponse.getData();

        // ── DB write: transaction opened + committed inside energyMetricDao.save() ──
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
        log.debug("[ENERGY] Saved EnergyMetric category={} targetId={} power={}W",
            category, targetId, dto.getPower());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // resetGateways — called daily at 00:00 by EnergyMetricResetJob
    // No @Transactional needed — no DB operations, only HTTP calls.
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void resetGateways() {
        List<ClientDto> gateways = clientService.getAllGateways();
        if (gateways == null || gateways.isEmpty()) {
            log.warn("[ENERGY-RESET] No gateways found, skipping reset");
            return;
        }

        log.info("[ENERGY-RESET] Starting daily energy reset for {} gateways", gateways.size());

        for (ClientDto gateway : gateways) {
            try {
                callResetWithRetry(gateway);
            } catch (Exception e) {
                log.error("[ENERGY-RESET] Unexpected error for gateway [{}]: {}",
                    gateway.username(), e.getMessage(), e);
            }
        }
    }

    private void callResetWithRetry(ClientDto gateway) {
        String url = UrlConstant.getEnergyResetV1(gateway.ipAddress());

        HttpClientUtil.Response response = HttpClientUtil.post(url, "");
        if (response.isSuccess()) {
            log.info("[ENERGY-RESET] Gateway [{}] reset OK", gateway.username());
            return;
        }

        // Retry once
        log.warn("[ENERGY-RESET] Gateway [{}] reset failed (status={}), retrying once...",
            gateway.username(), response.getStatusCode());

        try {
            Thread.sleep(RESET_RETRY_DELAY.toMillis());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        HttpClientUtil.Response retry = HttpClientUtil.post(url, "");
        if (retry.isSuccess()) {
            log.info("[ENERGY-RESET] Gateway [{}] reset OK on retry", gateway.username());
        } else {
            log.error("[ENERGY-RESET] Gateway [{}] reset FAILED after retry (status={})",
                gateway.username(), retry.getStatusCode());
        }
    }
}
