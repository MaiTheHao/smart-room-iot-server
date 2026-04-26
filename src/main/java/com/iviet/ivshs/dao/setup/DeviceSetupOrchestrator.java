package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.HardwareConfig;
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

@Slf4j(topic = "SETUP-ORCH")
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
        log.info("Init: Initialized with {} strategies", strategyMap.size());
    }

    public int persistAll(
        List<SetupRequest.BodyData.DeviceConfig> devices,
        Client client,
        Room room
    ) {
        if (devices == null || devices.isEmpty()) {
            log.warn("Persist: No devices to persist");
            return 0;
        }

        log.info("Persist: Starting persistence: count={}, roomId={}, clientId={}", 
            devices.size(), room.getId(), client.getId());

        int processedCount = 0;

        for (int i = 0; i < devices.size(); i++) {
            SetupRequest.BodyData.DeviceConfig device = devices.get(i);

            if (device.getCategory() == null) {
                log.error("Persist: Missing category at index {}: naturalId={}", i, device.getNaturalId());
                throw new InternalServerErrorException(
                    String.format("Device at index %d has no category: %s", i, device.getNaturalId())
                );
            }

            DeviceSetupStrategy strategy = strategyMap.get(device.getCategory());
            if (strategy == null) {
                log.error("Persist: No strategy for category {} at index {}: naturalId={}", 
                    device.getCategory(), i, device.getNaturalId());
                throw new InternalServerErrorException(
                    String.format("Unsupported device category '%s' for device: %s", 
                        device.getCategory(), device.getNaturalId())
                );
            }

            try {
                HardwareConfig hardwareConfig = createDeviceControl(device, room, client);
                strategy.persist(device, room, hardwareConfig);
                processedCount++;

                if (processedCount % BATCH_SIZE == 0) {
                    entityManager.flush();
                    entityManager.clear();
                    log.info("Persist: Batch checkpoint: {}/{}", processedCount, devices.size());
                }

            } catch (InternalServerErrorException e) {
                throw e;
            } catch (Exception e) {
                log.error("Persist: Failed at index {}: naturalId={}, category={}", 
                    i, device.getNaturalId(), device.getCategory(), e);
                throw new InternalServerErrorException(
                    String.format("Failed to persist device '%s' (category: %s): %s", 
                        device.getNaturalId(), device.getCategory(), e.getMessage()), 
                    e
                );
            }
        }

        entityManager.flush();
        log.info("Persist: All devices persisted successfully: count={}", processedCount);

        return processedCount;
    }

    private HardwareConfig createDeviceControl(
        SetupRequest.BodyData.DeviceConfig device,
        Room room,
        Client client
    ) {
        HardwareConfig dc = new HardwareConfig();
        dc.setControlType(device.getControlType());
        dc.setGpioPin(device.getGpioPin().getFirst());
        dc.setBleMacAddress(device.getBleMac());
        dc.setApiEndpoint(device.getApiEndpoint());
        dc.setClient(client);
        dc.setRoom(room);

        entityManager.persist(dc);
        entityManager.flush();
        log.debug("Create: Control created: id={}, type={}, naturalId={}", 
            dc.getId(), device.getControlType(), device.getNaturalId());

        return dc;
    }
}
