package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.entities.LightLan;
import com.iviet.ivshs.enumeration.ActuatorPower;

import lombok.Builder;

import com.iviet.ivshs.enumeration.DeviceCategory;

@Builder
public record LightDto(
    Long id,
    String naturalId,
    String name,
    String description,
    Boolean isActive,
    ActuatorPower power,
    Integer level,
    Long roomId,
    Long deviceControlId,
    DeviceCategory category
) {
    public LightDto(Long id, String naturalId, String name, String description, Boolean isActive, ActuatorPower power, Integer level, Long roomId, Long deviceControlId) {
        this(id, naturalId, name, description, isActive, power, level, roomId, deviceControlId, DeviceCategory.LIGHT);
    }

    public static LightDto from(Light entity, LightLan lan) {
        if (entity == null) {
            return null;
        }
        return LightDto.builder()
                .id(entity.getId())
                .naturalId(entity.getNaturalId())
                .name(lan != null ? lan.getName() : null)
                .description(lan != null ? lan.getDescription() : null)
                .isActive(entity.getIsActive())
                .power(entity.getPower())
                .level(entity.getLevel())
                .roomId((entity.getRoom() != null) ? entity.getRoom().getId() : null)
                .deviceControlId(entity.getHardwareConfig() != null ? entity.getHardwareConfig().getId() : null)
                .category(DeviceCategory.LIGHT)
                .build();
    }
}