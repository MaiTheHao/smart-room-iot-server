package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.FanGpio;
import com.iviet.ivshs.entities.FanIr;
import com.iviet.ivshs.entities.FanLan;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.enumeration.FanType;
import com.iviet.ivshs.exception.domain.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FanSetupStrategy extends AbstractDeviceSetupStrategy {

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.FAN;
    }

    @Override
    public void persist(
        SetupRequest.BodyData.DeviceConfig device,
        Room room,
        DeviceControl deviceControl
    ) {
        FanType fanType = device.getFanType();

        if (fanType == null) {
            log.warn("[SETUP:FAN] fanType is null for naturalId={}, defaulting to GPIO", device.getNaturalId());
            fanType = FanType.GPIO;
        }

        Fan fan = createFanByType(fanType);
        setupBaseIoTProperties(fan, device, room, deviceControl);
        attachTranslations(fan, device.getTranslations(), FanLan::new);
        entityManager.persist(fan);

        if (log.isDebugEnabled()) {
            log.debug("[SETUP:FAN] created: naturalId={}, type={}", device.getNaturalId(), fanType);
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
