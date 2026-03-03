package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.entities.LightLan;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.enumeration.DeviceCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LightSetupStrategy extends AbstractDeviceSetupStrategy {

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
        attachTranslations(light, device.getTranslations(), LightLan::new);
        entityManager.persist(light);
        if (log.isDebugEnabled()) log.debug("[SETUP:LIGHT] created: naturalId={}", device.getNaturalId());
    }
}
