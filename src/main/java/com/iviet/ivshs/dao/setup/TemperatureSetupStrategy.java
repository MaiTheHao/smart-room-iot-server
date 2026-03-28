package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dao.TemperatureDao;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.entities.Temperature;
import com.iviet.ivshs.entities.TemperatureLan;
import com.iviet.ivshs.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemperatureSetupStrategy extends AbstractDeviceSetupStrategy {

    private final TemperatureDao temperatureDao;

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.TEMPERATURE;
    }

    @Override
    public void persist(
        SetupRequest.BodyData.DeviceConfig device,
        Room room,
        DeviceControl deviceControl
    ) {
        Temperature temperature = new Temperature();
        setupBaseIoTProperties(temperature, device, room, deviceControl);
        entityManager.persist(temperature);
        entityManager.flush();
        attachTranslations(temperature, device.getTranslations(), TemperatureLan::new);
        log.debug("[TEMP] Device created: {}", device.getNaturalId());
    }

    @Override
    public void rollback(Long deviceId) {
        try {
            temperatureDao.deleteById(deviceId);
            log.debug("[TEMP] Rolled back: {}", deviceId);
        } catch (Exception e) {
            log.error("[TEMP] Rollback failed: {}", deviceId, e);
        }
    }
}
