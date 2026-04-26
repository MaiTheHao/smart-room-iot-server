package com.iviet.ivshs.dto;

import java.util.List;

import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.FanIr;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorState;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.enumeration.FanType;

import lombok.Builder;

import com.iviet.ivshs.enumeration.DeviceCategory;

@Builder
public record FanDto(
    Long id,
    String naturalId,
    String name,
    String description,
    Boolean isActive,
    Long roomId,
    ActuatorPower power,
    FanType type,
    Integer speed,
    ActuatorMode mode,
    ActuatorState light,
    ActuatorSwing swing,
    Long deviceControlId,
    DeviceCategory category
) {
    public FanDto(Long id, String naturalId, String name, String description, Boolean isActive, Long roomId, ActuatorPower power, FanType type, Integer speed, ActuatorMode mode, ActuatorState light, ActuatorSwing swing, Long deviceControlId) {
        this(id, naturalId, name, description, isActive, roomId, power, type, speed, mode, light, swing, deviceControlId, DeviceCategory.FAN);
    }

    public static FanDto from(Fan entity) {
        if (entity == null) return null;
        
        FanDtoBuilder builder = FanDto.builder()
                .id(entity.getId())
                .naturalId(entity.getNaturalId())
                .isActive(entity.getIsActive())
                .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
                .power(entity.getPower())
                .deviceControlId(entity.getHardwareConfig() != null ? entity.getHardwareConfig().getId() : null)
                .category(DeviceCategory.FAN);
                
        if (entity instanceof FanIr fanIr) {
            builder.type(FanType.IR)
									.speed(fanIr.getSpeed())
									.mode(fanIr.getMode())
									.light(fanIr.getLight())
									.swing(fanIr.getSwing());
        } else {
            builder.type(FanType.GPIO);
        }

        return builder.build();
    }

    public static List<FanDto> fromEntities(List<Fan> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(FanDto::from)
                .toList();
    }
}
