package com.iviet.ivshs.dto;

import java.util.List;
import java.util.Set;

import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.shared.enumeration.ActuatorMode;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.enumeration.ActuatorSwing;
import com.iviet.ivshs.shared.util.DeviceCapabilityRegistry;

import lombok.Builder;

import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.DeviceSpecificType;

@Builder
public record FanDto(
        Long id,
        String naturalId,
        String name,
        String description,
        Boolean isActive,
        Long roomId,
        ActuatorPower power,
        DeviceSpecificType specificType,
        Integer duration,
        Integer speed,
        ActuatorMode mode,
        ActuatorPower light,
        ActuatorSwing swing,
        Long deviceControlId,
        DeviceCategory category,
        Set<String> capabilities) {
    /**
     * Constructor cho JPQL projection query (DAO layer).
     * Thứ tự tham số phải khớp với thứ tự cột trong SELECT.
     */
    public FanDto(Long id, String naturalId, String name, String description, Boolean isActive, Long roomId,
            ActuatorPower power, DeviceSpecificType specificType, Integer duration, Integer speed, ActuatorMode mode,
            ActuatorPower light, ActuatorSwing swing, Long deviceControlId) {
        this(id, naturalId, name, description, isActive, roomId, power, specificType, duration, speed, mode, light, swing,
                deviceControlId, DeviceCategory.FAN, DeviceCapabilityRegistry.getCapabilities(DeviceCategory.FAN, specificType));
    }

    /**
     * JPQL projection fragment for use in DAO layer SELECT queries.
     * Thứ tự cột khớp với constructor {@link #FanDto(Long, String, String, String, Boolean, Long, ActuatorPower, DeviceSpecificType, Integer, Integer, ActuatorMode, ActuatorPower, ActuatorSwing, Long)}.
     *
     * @param fanAlias     alias của Fan entity (vd "f")
     * @param fanLangAlias alias của translations join (vd "tl")
     * @return comma-separated column list cho JPQL SELECT
     */
    public static String jpqlProjection(String fanAlias, String fanLangAlias) {
        return "%s.id, %s.naturalId, %s.name, %s.description, %s.isActive, %s.room.id, %s.power, %s.specificType, %s.duration, %s.speed, %s.mode, %s.light, %s.swing, %s.hardwareConfig.id"
                .formatted(
                        fanAlias, fanAlias, fanLangAlias, fanLangAlias,
                        fanAlias, fanAlias, fanAlias, fanAlias,
                        fanAlias, fanAlias, fanAlias, fanAlias,
                        fanAlias, fanAlias
                );
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
                .duration(entity.getDuration())
                .deviceControlId(entity.getHardwareConfig() != null ? entity.getHardwareConfig().getId() : null)
                .category(DeviceCategory.FAN)
                .capabilities(DeviceCapabilityRegistry.getCapabilities(DeviceCategory.FAN, entity.getSpecificType()));

        builder.speed(entity.getSpeed())
               .duration(entity.getDuration())
               .mode(entity.getMode())
               .swing(entity.getSwing())
               .light(entity.getLight());

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
