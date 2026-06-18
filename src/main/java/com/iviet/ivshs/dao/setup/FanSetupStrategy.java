package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dto.setup.SetupRequest;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.FanLan;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.DeviceSpecificType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FanSetupStrategy extends AbstractDeviceSetupStrategy {

    private final FanDao fanDao;

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.FAN;
    }

    @Override
    public void persist(SetupRequest.BodyData.DeviceConfig device, Room room, HardwareConfig hardwareConfig) {
        DeviceSpecificType fanType = DeviceSpecificType.fromString(device.getSpecificType());

        if (fanType == null) {
            log.warn("Create: Type unknown, defaulting to GPIO: {}", device.getNaturalId());
            fanType = DeviceSpecificType.GPIO;
            device.setSpecificType(fanType.name());
        }

        Fan fan = createFanByType(fanType);
        setupBaseIoTProperties(fan, device, room, hardwareConfig);
        fan.setDuration(device.getDuration());
        entityManager.persist(fan);
        entityManager.flush();
        attachTranslations(fan, device.getTranslations(), FanLan::new);
        log.debug("Create: Device created: {}, type: {}", device.getNaturalId(), fanType);
    }

    @Override
    public void rollback(Long deviceId) {
        try {
            fanDao.deleteById(deviceId);
            log.debug("Rollback: Rolled back: {}", deviceId);
        } catch (Exception e) {
            log.error("Rollback: Failed for {}: {}", deviceId, e.getMessage(), e);
        }
    }

    private Fan createFanByType(DeviceSpecificType fanType) {
        return new Fan();
    }
}
