package com.iviet.ivshs.dto;

import java.util.List;

import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.enumeration.AcMode;
import com.iviet.ivshs.enumeration.AcPower;
import com.iviet.ivshs.enumeration.AcSwing;

import lombok.Builder;

@Builder
public record AirConditionDto(
    Long id,
    String naturalId,
    String name,
    String description,
    Boolean isActive,
    Long roomId,
    AcPower power,
    Integer temperature,
    AcMode mode,
    Integer fanSpeed,
    AcSwing swing
) {

    public static AirConditionDto fromEntity(AirCondition entity) {
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
                .build();
    }

    public static List<AirConditionDto> fromEntities(List<AirCondition> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(AirConditionDto::fromEntity)
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