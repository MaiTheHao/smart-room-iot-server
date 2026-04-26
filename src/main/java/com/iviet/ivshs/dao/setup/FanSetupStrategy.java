package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.FanGpio;
import com.iviet.ivshs.entities.FanIr;
import com.iviet.ivshs.entities.FanLan;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.enumeration.FanType;
import com.iviet.ivshs.exception.domain.BadRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j(topic = "SETUP-FAN")
@Component
@RequiredArgsConstructor
public class FanSetupStrategy extends AbstractDeviceSetupStrategy {

    private final FanDao fanDao;

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.FAN;
    }

    @Override
    public void persist(
        SetupRequest.BodyData.DeviceConfig device,
        Room room,
        HardwareConfig hardwareConfig
    ) {
        FanType fanType = FanType.fromString(device.getSpecificType());

        if (fanType == null) {
            log.warn("Create: Type unknown, defaulting to GPIO: {}", device.getNaturalId());
            fanType = FanType.GPIO;
        }

        Fan fan = createFanByType(fanType);
        setupBaseIoTProperties(fan, device, room, hardwareConfig);
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

    private Fan createFanByType(FanType fanType) {
        return switch (fanType) {
            case IR   -> new FanIr();
            case GPIO -> new FanGpio();
            default   -> throw new BadRequestException("Unsupported fan type for setup: " + fanType);
        };
    }
}
