package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dao.Co2SensorDao;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.entities.Co2Sensor;
import com.iviet.ivshs.entities.Co2SensorLan;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Co2SetupStrategy extends AbstractDeviceSetupStrategy {

    private final Co2SensorDao co2SensorDao;

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.SENSOR_CO2;
    }

    @Override
    public void persist(
            SetupRequest.BodyData.DeviceConfig device,
            Room room,
            HardwareConfig hardwareConfig) {
        Co2Sensor co2Sensor = new Co2Sensor();
        setupBaseIoTProperties(co2Sensor, device, room, hardwareConfig);
        entityManager.persist(co2Sensor);
        entityManager.flush();
        attachTranslations(co2Sensor, device.getTranslations(), Co2SensorLan::new);
        log.debug("Create: Device created: {}", device.getNaturalId());
    }

    @Override
    public void rollback(Long deviceId) {
        try {
            co2SensorDao.deleteById(deviceId);
            log.debug("Rollback: Rolled back: {}", deviceId);
        } catch (Exception e) {
            log.error("Rollback: Failed for {}: {}", deviceId, e.getMessage(), e);
        }
    }
}
