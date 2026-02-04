package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Floor;
import com.iviet.ivshs.entities.FloorLan;

import lombok.Builder;

@Builder
public record FloorDto(
    Long id,
    String name,
    String code,
    String description,
    Integer level
) {
    public static FloorDto fromEntity(Floor entity, FloorLan lan) {
        return FloorDto.builder()
            .id(entity.getId())
            .code(entity.getCode())
            .name(lan.getName())
            .description(lan.getDescription())
            .level(entity.getLevel())
            .build();
    }

    public static Floor toEntity(FloorDto dto) {
        Floor floor = new Floor();
        floor.setId(dto.id());
        floor.setCode(dto.code());
        floor.setLevel(dto.level());
        return floor;
    }
}