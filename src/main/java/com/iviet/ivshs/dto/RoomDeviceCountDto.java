package com.iviet.ivshs.dto;

public record RoomDeviceCountDto(
    Long roomId,
    Long lightCount,
    Long acCount,
    Long fanCount
) {
    public static String jpqlProjection(String roomAlias) {
        return "%s.id, (SELECT COUNT(l) FROM Light l WHERE l.room.id = %s.id), (SELECT COUNT(ac) FROM AirCondition ac WHERE ac.room.id = %s.id), (SELECT COUNT(f) FROM Fan f WHERE f.room.id = %s.id)"
            .formatted(roomAlias, roomAlias, roomAlias, roomAlias);
    }
}
