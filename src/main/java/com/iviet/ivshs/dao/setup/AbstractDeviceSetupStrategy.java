package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dto.setup.SetupRequest;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.entities.base.BaseIoTDevice;
import com.iviet.ivshs.entities.base.BaseIoTEntity;
import com.iviet.ivshs.entities.base.BaseTranslatableEntity;
import com.iviet.ivshs.entities.base.BaseTranslation;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.enumeration.DeviceSpecificType;
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
            HardwareConfig hardwareConfig) {
        entity.setIsActive(device.isActive());
        entity.setNaturalId(device.getNaturalId());
        entity.setSpecificType(DeviceSpecificType.fromString(device.getSpecificType()));
        entity.setRoom(room);
        entity.setHardwareConfig(hardwareConfig);

        if (entity instanceof BaseIoTDevice<?> actuator) {
            actuator.setPower(ActuatorPower.OFF);
        }
    }

    protected <T extends BaseTranslatableEntity<L>, L extends BaseTranslation<T>> void attachTranslations(
            T entity,
            Map<String, SetupRequest.BodyData.DeviceConfig.TranslationDetail> translations,
            Supplier<L> translationSupplier) {
        if (translations == null || translations.isEmpty())
            return;

        translations.forEach((langCode, detail) -> {
            L translation = translationSupplier.get();
            translation.setLangCode(langCode);
            translation.setName(detail.getName());
            translation.setDescription(detail.getDescription());
            entity.addTranslation(translation);
        });
    }
}
