package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.enumeration.DeviceCategory;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceSetupOrchestrator {

    private static final int BATCH_SIZE = 50;

    private final List<DeviceSetupStrategy> strategies;

    @PersistenceContext
    private EntityManager entityManager;

    private Map<DeviceCategory, DeviceSetupStrategy> strategyMap;

    @PostConstruct
    private void init() {
        strategyMap = new EnumMap<>(DeviceCategory.class);
        strategies.forEach(s -> strategyMap.put(s.getSupportedCategory(), s));
        log.info("[SETUP:ORCH] Initialized with {} strategies: {}", strategyMap.size(), strategyMap.keySet());
    }

    public int persistAll(
        List<SetupRequest.BodyData.DeviceConfig> devices,
        Client client,
        Room room
    ) {
        if (devices == null || devices.isEmpty()) {
            log.warn("[SETUP:ORCH] No devices to persist for roomId={}", room.getId());
            return 0;
        }

        log.info("[SETUP:ORCH] Starting persistence: total={}, roomId={}, clientId={}", 
            devices.size(), room.getId(), client.getId());

        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < devices.size(); i++) {
            SetupRequest.BodyData.DeviceConfig device = devices.get(i);

            if (device.getCategory() == null) {
                log.warn("[SETUP:ORCH:SKIP] category is null: index={}, naturalId={}", i, device.getNaturalId());
                failCount++;
                continue;
            }

            DeviceSetupStrategy strategy = strategyMap.get(device.getCategory());

            if (strategy == null) {
                log.warn("[SETUP:ORCH:SKIP] No strategy for category={}: index={}, naturalId={}", 
                    device.getCategory(), i, device.getNaturalId());
                failCount++;
                continue;
            }

            try {
                DeviceControl deviceControl = createDeviceControl(device, room, client);
                strategy.persist(device, room, deviceControl);
                successCount++;

                if (successCount % BATCH_SIZE == 0) {
                    entityManager.flush();
                    entityManager.clear();
                    log.info("[SETUP:ORCH:BATCH] Flushed: processed={}/{}", successCount, devices.size());
                }

            } catch (Exception e) {
                failCount++;
                log.error("[SETUP:ORCH:ERROR] Failed: index={}, naturalId={}, category={}: {}", 
                    i, device.getNaturalId(), device.getCategory(), e.getMessage(), e);
            }
        }

        entityManager.flush();

        log.info("[SETUP:ORCH] Done: total={}, success={}, failed={}, roomId={}", 
            devices.size(), successCount, failCount, room.getId());

        return successCount;
    }

    private DeviceControl createDeviceControl(
        SetupRequest.BodyData.DeviceConfig device,
        Room room,
        Client client
    ) {
        DeviceControl dc = new DeviceControl();
        dc.setDeviceControlType(device.getControlType());
        dc.setGpioPin(device.getGpioPin());
        dc.setBleMacAddress(device.getBleMac());
        dc.setApiEndpoint(device.getApiEndpoint());
        dc.setClient(client);
        dc.setRoom(room);

        entityManager.persist(dc);
        entityManager.flush();

        if (log.isDebugEnabled()) {
            log.debug("[SETUP:ORCH:CTRL] DeviceControl created: id={}, type={}, naturalId={}", 
                dc.getId(), device.getControlType(), device.getNaturalId());
        }

        return dc;
    }
}
