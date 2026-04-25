package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.PowerConsumption;
import com.iviet.ivshs.entities.PowerConsumptionLan;
import com.iviet.ivshs.enumeration.DeviceCategory;
import lombok.Builder;

@Builder
public record PowerConsumptionDto(
    Long id,
    String name,
    String description,
    Boolean isActive,
    Double currentWatt,
    String naturalId,
    Long roomId,
    Long deviceControlId,
    DeviceCategory category
) {
    public PowerConsumptionDto(Long id, String name, String description, Boolean isActive, Double currentWatt, String naturalId, Long roomId, Long deviceControlId) {
        this(id, name, description, isActive, currentWatt, naturalId, roomId, deviceControlId, DeviceCategory.POWER_CONSUMPTION);
    }

    public static PowerConsumptionDto from(PowerConsumption entity, PowerConsumptionLan powerLan) {
        return PowerConsumptionDto.builder()
            .id(entity.getId())
            .name(powerLan.getName())
            .description(powerLan.getDescription())
            .isActive(entity.getIsActive())
            .currentWatt(entity.getCurrentWatt())
            .naturalId(entity.getNaturalId())
            .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
            .deviceControlId(entity.getDeviceControl() != null ? entity.getDeviceControl().getId() : null)
            .category(DeviceCategory.POWER_CONSUMPTION)
            .build();
    }
}
