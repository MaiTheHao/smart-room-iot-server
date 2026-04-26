package com.iviet.ivshs.dto;

import java.util.List;

import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.enumeration.ActuatorPower;

import lombok.Builder;

import com.iviet.ivshs.enumeration.DeviceCategory;

@Builder
public record AirConditionDto(
    Long id,
    String naturalId,
    String name,
    String description,
    Boolean isActive,
    Long roomId,
    ActuatorPower power,
    Integer temperature,
    ActuatorMode mode,
    Integer fanSpeed,
    ActuatorSwing swing,
    Long deviceControlId,
    DeviceCategory category
) {
    public AirConditionDto(Long id, String naturalId, String name, String description, Boolean isActive, Long roomId, ActuatorPower power, Integer temperature, ActuatorMode mode, Integer fanSpeed, ActuatorSwing swing, Long deviceControlId) {
        this(id, naturalId, name, description, isActive, roomId, power, temperature, mode, fanSpeed, swing, deviceControlId, DeviceCategory.AIR_CONDITION);
    }

    public static AirConditionDto from(AirCondition entity) {
        if (entity == null) return null;
        return AirConditionDto.builder()
                .id(entity.getId())
                .naturalId(entity.getNaturalId())
                .isActive(entity.getIsActive())
                .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
                .power(entity.getPower())
                .temperature(entity.getTemperature())
                .mode(entity.getMode())
                .fanSpeed(entity.getFanSpeed())
                .swing(entity.getSwing())
                .deviceControlId(entity.getHardwareConfig() != null ? entity.getHardwareConfig().getId() : null)
                .name(entity.getTranslations().isEmpty() ? null : entity.getTranslations().iterator().next().getName())
                .description(entity.getTranslations().isEmpty() ? null : entity.getTranslations().iterator().next().getDescription())
                .category(DeviceCategory.AIR_CONDITION)
                .build();
    }

    public static List<AirConditionDto> fromEntities(List<AirCondition> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(AirConditionDto::from)
                .toList();
    }

    public static AirCondition toEntity(AirConditionDto dto) {
        if (dto == null) return null;
        AirCondition entity = new AirCondition();
        entity.setId(dto.id());
        entity.setNaturalId(dto.naturalId());
        entity.setIsActive(dto.isActive());
        entity.setPower(dto.power());
        entity.setTemperature(dto.temperature());
        entity.setMode(dto.mode());
        entity.setFanSpeed(dto.fanSpeed());
        entity.setSwing(dto.swing());
        return entity;
    }
}