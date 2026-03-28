package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.enumeration.ActuatorPower;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractDeviceSetupStrategy implements DeviceSetupStrategy {

    @PersistenceContext
    protected EntityManager entityManager;

    protected void setupBaseIoTProperties(
        BaseIoTEntity<?> entity,
        SetupRequest.BodyData.DeviceConfig device,
        Room room,
        DeviceControl deviceControl
    ) {
        entity.setIsActive(device.isActive());
        entity.setNaturalId(device.getNaturalId());
        entity.setRoom(room);
        entity.setDeviceControl(deviceControl);

        if (entity instanceof BaseIoTActuator<?> actuator) {
            actuator.setPower(ActuatorPower.OFF);
        }
    }

    protected <T extends BaseTranslatableEntity<L>, L extends BaseTranslation<T>> void attachTranslations(
        T entity,
        Map<String, SetupRequest.BodyData.DeviceConfig.TranslationDetail> translations,
        Supplier<L> translationSupplier
    ) {
        if (translations == null || translations.isEmpty()) return;

        translations.forEach((langCode, detail) -> {
            L translation = translationSupplier.get();
            translation.setLangCode(langCode);
            translation.setName(detail.getName());
            translation.setDescription(detail.getDescription());
            entity.addTranslation(translation);
        });
    }
}
