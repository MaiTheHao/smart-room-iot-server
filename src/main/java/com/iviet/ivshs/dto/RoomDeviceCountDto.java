package com.iviet.ivshs.dto;

public record RoomDeviceCountDto(
    Long roomId,
    Long lightCount,
    Long acCount,
    Long fanCount
) {
}
