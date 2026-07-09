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
    Long floorId,
    Long version
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
            (entity.getFloor() != null) ? entity.getFloor().getId() : null,
            entity.getVersion()
        );
    }

    public static String jpqlProjection(String roomAlias, String roomLangAlias) {
        return "%s.id, %s.code, %s.name, %s.description, %s.floor.id, %s.version"
            .formatted(roomAlias, roomAlias, roomLangAlias, roomLangAlias, roomAlias, roomAlias);
    }
}