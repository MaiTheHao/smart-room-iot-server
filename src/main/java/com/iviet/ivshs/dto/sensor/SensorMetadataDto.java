package com.iviet.ivshs.dto.sensor;

import com.iviet.ivshs.entities.base.BaseIoTSensor;
import com.iviet.ivshs.entities.base.BaseTranslation;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public record SensorMetadataDto(
    Long id,
    String naturalId,
    String name,
    String description,
    Boolean isActive,
    Long roomId,
    DeviceCategory category,
    SensorSpecificData data
) {
    public static SensorMetadataDto from(BaseIoTSensor<?> entity, BaseTranslation<?> lan) {
        return new SensorMetadataDto(
            entity.getId(), entity.getNaturalId(), lan.getName(), lan.getDescription(),
            entity.getIsActive(), entity.getRoom().getId(), entity.getCategory(),
            entity.extractBusinessData()
        );
    }
}
