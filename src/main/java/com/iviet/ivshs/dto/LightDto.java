package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.entities.LightLan;
import lombok.Builder;

@Builder
public record LightDto(
    Long id,
    String naturalId,
    String name,
    String description,
    Boolean isActive,
    Integer level,
    Long roomId
) {
    public static LightDto from(Light entity, LightLan lan) {
        if (entity == null) {
            return null;
        }
        return new LightDto(
            entity.getId(),
            entity.getNaturalId(),
            lan != null ? lan.getName() : null,
            lan != null ? lan.getDescription() : null,
            entity.getIsActive(),
            entity.getLevel(),
            (entity.getRoom() != null) ? entity.getRoom().getId() : null
        );
    }
}