package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dao.TemperatureDao;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.entities.Temperature;
import com.iviet.ivshs.entities.TemperatureLan;
import com.iviet.ivshs.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j(topic = "SETUP-TEMP")
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
        HardwareConfig hardwareConfig
    ) {
        Temperature temperature = new Temperature();
        setupBaseIoTProperties(temperature, device, room, hardwareConfig);
        entityManager.persist(temperature);
        entityManager.flush();
        attachTranslations(temperature, device.getTranslations(), TemperatureLan::new);
        log.debug("Create: Device created: {}", device.getNaturalId());
    }

    @Override
    public void rollback(Long deviceId) {
        try {
            temperatureDao.deleteById(deviceId);
            log.debug("Rollback: Rolled back: {}", deviceId);
        } catch (Exception e) {
            log.error("Rollback: Failed for {}: {}", deviceId, e.getMessage(), e);
        }
    }
}
