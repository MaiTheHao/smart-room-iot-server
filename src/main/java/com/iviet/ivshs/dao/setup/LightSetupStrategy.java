package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.entities.LightLan;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j(topic = "SETUP-LIGHT")
@Component
@RequiredArgsConstructor
public class LightSetupStrategy extends AbstractDeviceSetupStrategy {

    private final LightDao lightDao;

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.LIGHT;
    }

    @Override
    public void persist(
        SetupRequest.BodyData.DeviceConfig device,
        Room room,
        DeviceControl deviceControl
    ) {
        Light light = new Light();
        setupBaseIoTProperties(light, device, room, deviceControl);
        entityManager.persist(light);
        entityManager.flush();
        attachTranslations(light, device.getTranslations(), LightLan::new);
        log.debug("Create: Device created: {}", device.getNaturalId());
    }

    @Override
    public void rollback(Long deviceId) {
        try {
            lightDao.deleteById(deviceId);
            log.debug("Rollback: Rolled back: {}", deviceId);
        } catch (Exception e) {
            log.error("Rollback: Failed for {}: {}", deviceId, e.getMessage(), e);
        }
    }
}
