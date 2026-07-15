package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dao.HumiditySensorDao;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.entities.HumiditySensor;
import com.iviet.ivshs.entities.HumiditySensorLan;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HumiditySetupStrategy extends AbstractDeviceSetupStrategy {

    private final HumiditySensorDao humiditySensorDao;

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.HUMIDITY;
    }

    @Override
    public void persist(
            SetupRequest.BodyData.DeviceConfig device,
            Room room,
            HardwareConfig hardwareConfig) {
        HumiditySensor humiditySensor = new HumiditySensor();
        setupBaseIoTProperties(humiditySensor, device, room, hardwareConfig);
        entityManager.persist(humiditySensor);
        entityManager.flush();
        attachTranslations(humiditySensor, device.getTranslations(), HumiditySensorLan::new);
        log.debug("Create: Device created: {}", device.getNaturalId());
    }

    @Override
    public void rollback(Long deviceId) {
        try {
            humiditySensorDao.deleteById(deviceId);
            log.debug("Rollback: Rolled back: {}", deviceId);
        } catch (Exception e) {
            log.error("Rollback: Failed for {}: {}", deviceId, e.getMessage(), e);
        }
    }
}
