package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.PowerConsumption;
import com.iviet.ivshs.entities.PowerConsumptionLan;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.enumeration.DeviceCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PowerConsumptionSetupStrategy extends AbstractDeviceSetupStrategy {

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.POWER_CONSUMPTION;
    }

    @Override
    public void persist(
        SetupRequest.BodyData.DeviceConfig device,
        Room room,
        DeviceControl deviceControl
    ) {
        PowerConsumption pc = new PowerConsumption();
        setupBaseIoTProperties(pc, device, room, deviceControl);
        attachTranslations(pc, device.getTranslations(), PowerConsumptionLan::new);
        entityManager.persist(pc);
        if (log.isDebugEnabled()) log.debug("[SETUP:POWER] created: naturalId={}", device.getNaturalId());
    }
}
