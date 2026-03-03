package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.entities.Temperature;
import com.iviet.ivshs.entities.TemperatureLan;
import com.iviet.ivshs.enumeration.DeviceCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TemperatureSetupStrategy extends AbstractDeviceSetupStrategy {

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
        attachTranslations(temperature, device.getTranslations(), TemperatureLan::new);
        entityManager.persist(temperature);
        if (log.isDebugEnabled()) log.debug("[SETUP:TEMP] created: naturalId={}", device.getNaturalId());
    }
}
