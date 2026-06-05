package com.iviet.ivshs.dto.room;

public record RoomDeviceCountDto(
    Long roomId,
    Long lightCount,
    Long acCount,
    Long fanCount
) {
}
