package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Temperature;
import com.iviet.ivshs.entities.TemperatureLan;
import com.iviet.ivshs.enumeration.DeviceCategory;
import lombok.Builder;

@Builder
public record TemperatureDto(
    Long id,
    String name,
    String description,
    Boolean isActive,
    Double currentValue,
    String naturalId,
    Long roomId,
    Long deviceControlId,
    DeviceCategory category
) {
    public TemperatureDto(Long id, String name, String description, Boolean isActive, Double currentValue, String naturalId, Long roomId, Long deviceControlId) {
        this(id, name, description, isActive, currentValue, naturalId, roomId, deviceControlId, DeviceCategory.TEMPERATURE);
    }

    public static TemperatureDto from(Temperature entity, TemperatureLan temperatureLan) {
        return TemperatureDto.builder()
            .id(entity.getId())
            .name(temperatureLan.getName())
            .description(temperatureLan.getDescription())
            .isActive(entity.getIsActive())
            .currentValue(entity.getCurrentValue())
            .naturalId(entity.getNaturalId())
            .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
            .deviceControlId(entity.getDeviceControl() != null ? entity.getDeviceControl().getId() : null)
            .category(DeviceCategory.TEMPERATURE)
            .build();
    }
}