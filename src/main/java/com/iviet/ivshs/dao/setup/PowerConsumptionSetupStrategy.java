package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dao.PowerConsumptionDao;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.PowerConsumption;
import com.iviet.ivshs.entities.PowerConsumptionLan;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j(topic = "SETUP-POWER")
@Component
@RequiredArgsConstructor
public class PowerConsumptionSetupStrategy extends AbstractDeviceSetupStrategy {

    private final PowerConsumptionDao powerConsumptionDao;

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.POWER_CONSUMPTION;
    }

    @Override
    public void persist(
        SetupRequest.BodyData.DeviceConfig device,
        Room room,
        HardwareConfig hardwareConfig
    ) {
        PowerConsumption pc = new PowerConsumption();
        setupBaseIoTProperties(pc, device, room, hardwareConfig);
        entityManager.persist(pc);
        entityManager.flush();
        attachTranslations(pc, device.getTranslations(), PowerConsumptionLan::new);
        log.debug("Create: Device created: {}", device.getNaturalId());
    }

    @Override
    public void rollback(Long deviceId) {
        try {
            powerConsumptionDao.deleteById(deviceId);
            log.debug("Rollback: Rolled back: {}", deviceId);
        } catch (Exception e) {
            log.error("Rollback: Failed for {}: {}", deviceId, e.getMessage(), e);
        }
    }
}
