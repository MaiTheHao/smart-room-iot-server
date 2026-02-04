package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.entities.RoomLan;
import lombok.Builder;

@Builder
public record RoomDto(
    Long id,
    String code,
    String name,
    String description,
    Long floorId
) {
    public static RoomDto from(Room entity, RoomLan lan) {
        if (entity == null) {
            return null;
        }
        return new RoomDto(
            entity.getId(),
            entity.getCode(),
            lan != null ? lan.getName() : null,
            lan != null ? lan.getDescription() : null,
            (entity.getFloor() != null) ? entity.getFloor().getId() : null
        );
    }
}