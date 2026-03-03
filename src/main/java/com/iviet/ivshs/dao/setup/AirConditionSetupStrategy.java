package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.entities.AirConditionLan;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.enumeration.DeviceCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AirConditionSetupStrategy extends AbstractDeviceSetupStrategy {

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.AIR_CONDITION;
    }

    @Override
    public void persist(
        SetupRequest.BodyData.DeviceConfig device,
        Room room,
        DeviceControl deviceControl
    ) {
        AirCondition ac = new AirCondition();
        setupBaseIoTProperties(ac, device, room, deviceControl);

        ac.setTemperature(26);
        ac.setMode(ActuatorMode.COOL);
        ac.setFanSpeed(1);
        ac.setSwing(ActuatorSwing.OFF);

        attachTranslations(ac, device.getTranslations(), AirConditionLan::new);
        entityManager.persist(ac);
        if (log.isDebugEnabled()) log.debug("[SETUP:AC] created: naturalId={}", device.getNaturalId());
    }
}
