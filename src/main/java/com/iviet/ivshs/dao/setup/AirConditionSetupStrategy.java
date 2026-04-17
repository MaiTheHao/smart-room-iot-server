package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.entities.AirConditionLan;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j(topic = "SETUP-AC")
@Component
@RequiredArgsConstructor
public class AirConditionSetupStrategy extends AbstractDeviceSetupStrategy {
    
    private final AirConditionDao airConditionDao;

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
        entityManager.persist(ac);
        entityManager.flush();
        attachTranslations(ac, device.getTranslations(), AirConditionLan::new);
        log.debug("Create: Device created: {}", device.getNaturalId());
    }

    @Override
    public void rollback(Long deviceId) {
        try {
            airConditionDao.deleteById(deviceId);
            log.debug("Rollback: Rolled back: {}", deviceId);
        } catch (Exception e) {
            log.error("Rollback: Failed for {}: {}", deviceId, e.getMessage(), e);
        }
    }
}
