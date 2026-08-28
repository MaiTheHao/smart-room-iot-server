package com.iviet.ivshs.dto;

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
            entity.getId(),
            entity.getNaturalId(),
            lan != null ? lan.getName() : null,
            lan != null ? lan.getDescription() : null,
            entity.getIsActive(),
            entity.getRoom() != null ? entity.getRoom().getId() : null,
            entity.getCategory(),
            entity.extractBusinessData()
        );
    }
}
