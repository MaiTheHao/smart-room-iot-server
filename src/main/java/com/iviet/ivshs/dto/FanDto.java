package com.iviet.ivshs.dto;

import java.util.List;

import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.FanIr;
import com.iviet.ivshs.shared.enumeration.ActuatorMode;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.enumeration.ActuatorSwing;

import lombok.Builder;

import com.iviet.ivshs.shared.enumeration.DeviceCategory;

@Builder
public record FanDto(
        Long id,
        String naturalId,
        String name,
        String description,
        Boolean isActive,
        Long roomId,
        ActuatorPower power,
        String specificType,
        Integer speed,
        ActuatorMode mode,
        ActuatorPower light,
        ActuatorSwing swing,
        Long deviceControlId,
        DeviceCategory category) {
    /**
     * Constructor cho JPQL projection query (DAO layer).
     * Thứ tự tham số phải khớp với thứ tự cột trong SELECT.
     */
    public FanDto(Long id, String naturalId, String name, String description, Boolean isActive, Long roomId,
            ActuatorPower power, String specificType, Integer speed, ActuatorMode mode,
            ActuatorPower light, ActuatorSwing swing, Long deviceControlId) {
        this(id, naturalId, name, description, isActive, roomId, power, specificType, speed, mode, light, swing,
                deviceControlId, DeviceCategory.FAN);
    }

    /**
     * Factory method từ entity — dùng khi load entity trực tiếp (không qua
     * projection).
     */
    public static FanDto from(Fan entity) {
        if (entity == null)
            return null;

        FanDtoBuilder builder = FanDto.builder()
                .id(entity.getId())
                .naturalId(entity.getNaturalId())
                .isActive(entity.getIsActive())
                .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
                .power(entity.getPower())
                .specificType(entity.getSpecificType())
                .deviceControlId(entity.getHardwareConfig() != null ? entity.getHardwareConfig().getId() : null)
                .category(DeviceCategory.FAN);

        if (entity instanceof FanIr fanIr) {
            builder.speed(fanIr.getSpeed())
                    .mode(fanIr.getMode())
                    .light(fanIr.getLight())
                    .swing(fanIr.getSwing());
        }

        return builder.build();
    }

    public static List<FanDto> fromEntities(List<Fan> entities) {
        if (entities == null)
            return List.of();
        return entities.stream()
                .map(FanDto::from)
                .toList();
    }
}
