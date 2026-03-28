package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.exception.domain.InternalServerErrorException;

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
        log.info("[ORCH] Initialized with {} strategies", strategyMap.size());
    }

    public int persistAll(
        List<SetupRequest.BodyData.DeviceConfig> devices,
        Client client,
        Room room
    ) {
        if (devices == null || devices.isEmpty()) {
            log.warn("[ORCH] No devices to persist");
            return 0;
        }

        log.info("[ORCH] Starting persistence: count={}, roomId={}, clientId={}", 
            devices.size(), room.getId(), client.getId());

        int processedCount = 0;

        for (int i = 0; i < devices.size(); i++) {
            SetupRequest.BodyData.DeviceConfig device = devices.get(i);

            if (device.getCategory() == null) {
                log.error("[ORCH] Missing category at index {}: naturalId={}", i, device.getNaturalId());
                throw new InternalServerErrorException(
                    String.format("Device at index %d has no category: %s", i, device.getNaturalId())
                );
            }

            DeviceSetupStrategy strategy = strategyMap.get(device.getCategory());
            if (strategy == null) {
                log.error("[ORCH] No strategy for category {} at index {}: naturalId={}", 
                    device.getCategory(), i, device.getNaturalId());
                throw new InternalServerErrorException(
                    String.format("Unsupported device category '%s' for device: %s", 
                        device.getCategory(), device.getNaturalId())
                );
            }

            try {
                DeviceControl deviceControl = createDeviceControl(device, room, client);
                strategy.persist(device, room, deviceControl);
                processedCount++;

                if (processedCount % BATCH_SIZE == 0) {
                    entityManager.flush();
                    entityManager.clear();
                    log.info("[ORCH] Batch checkpoint: {}/{}", processedCount, devices.size());
                }

            } catch (InternalServerErrorException e) {
                throw e;
            } catch (Exception e) {
                log.error("[ORCH] Persistence failed at index {}: naturalId={}, category={}", 
                    i, device.getNaturalId(), device.getCategory(), e);
                throw new InternalServerErrorException(
                    String.format("Failed to persist device '%s' (category: %s): %s", 
                        device.getNaturalId(), device.getCategory(), e.getMessage()), 
                    e
                );
            }
        }

        entityManager.flush();
        log.info("[ORCH] All devices persisted successfully: count={}", processedCount);

        return processedCount;
    }

    private DeviceControl createDeviceControl(
        SetupRequest.BodyData.DeviceConfig device,
        Room room,
        Client client
    ) {
        DeviceControl dc = new DeviceControl();
        dc.setDeviceControlType(device.getControlType());
        dc.setGpioPin(device.getGpioPin().getFirst());
        dc.setBleMacAddress(device.getBleMac());
        dc.setApiEndpoint(device.getApiEndpoint());
        dc.setClient(client);
        dc.setRoom(room);

        entityManager.persist(dc);
        entityManager.flush();
        log.debug("[ORCH] Control created: id={}, type={}, naturalId={}", 
            dc.getId(), device.getControlType(), device.getNaturalId());

        return dc;
    }
}
