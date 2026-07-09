package com.iviet.ivshs.dto;

import java.util.List;
import java.util.Set;

import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.shared.enumeration.ActuatorMode;
import com.iviet.ivshs.shared.enumeration.ActuatorSwing;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.util.DeviceCapabilityRegistry;

import lombok.Builder;

import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.DeviceSpecificType;

@Builder
public record AirConditionDto(
        Long id,
        String naturalId,
        String name,
        String description,
        Boolean isActive,
        Long roomId,
        ActuatorPower power,
        DeviceSpecificType specificType,
        Integer duration,
        Integer temperature,
        ActuatorMode mode,
        Integer fanSpeed,
        ActuatorSwing swing,
        Long deviceControlId,
        DeviceCategory category,
        Set<String> capabilities) {
    /**
     * Constructor cho JPQL projection query (DAO layer).
     * Thứ tự tham số phải khớp với thứ tự cột trong SELECT.
     */
    public AirConditionDto(Long id, String naturalId, String name, String description, Boolean isActive, Long roomId,
            ActuatorPower power, DeviceSpecificType specificType, Integer duration, Integer temperature, ActuatorMode mode,
            Integer fanSpeed, ActuatorSwing swing, Long deviceControlId) {
        this(id, naturalId, name, description, isActive, roomId, power, specificType, duration, temperature, mode,
                fanSpeed, swing, deviceControlId, DeviceCategory.AIR_CONDITION, DeviceCapabilityRegistry.getCapabilities(DeviceCategory.AIR_CONDITION, specificType));
    }

    public static AirConditionDto from(AirCondition entity) {
        if (entity == null)
            return null;
        return AirConditionDto.builder()
                .id(entity.getId())
                .naturalId(entity.getNaturalId())
                .isActive(entity.getIsActive())
                .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
                .power(entity.getPower())
                .specificType(entity.getSpecificType())
                .duration(entity.getDuration())
                .temperature(entity.getTemperature())
                .mode(entity.getMode())
                .fanSpeed(entity.getFanSpeed())
                .swing(entity.getSwing())
                .deviceControlId(entity.getHardwareConfig() != null ? entity.getHardwareConfig().getId() : null)
                .name(entity.getTranslations().isEmpty() ? null : entity.getTranslations().iterator().next().getName())
                .description(entity.getTranslations().isEmpty() ? null
                        : entity.getTranslations().iterator().next().getDescription())
                .category(DeviceCategory.AIR_CONDITION)
                .capabilities(DeviceCapabilityRegistry.getCapabilities(DeviceCategory.AIR_CONDITION, entity.getSpecificType()))
                .build();
    }

    public static List<AirConditionDto> fromEntities(List<AirCondition> entities) {
        if (entities == null)
            return List.of();
        return entities.stream()
                .map(AirConditionDto::from)
                .toList();
    }

    public static AirCondition toEntity(AirConditionDto dto) {
        if (dto == null)
            return null;
        AirCondition entity = new AirCondition();
        entity.setId(dto.id());
        entity.setNaturalId(dto.naturalId());
        entity.setIsActive(dto.isActive());
        entity.setPower(dto.power());
        entity.setDuration(dto.duration());
        entity.setTemperature(dto.temperature());
        entity.setMode(dto.mode());
        entity.setFanSpeed(dto.fanSpeed());
        entity.setSwing(dto.swing());
        return entity;
    }
}