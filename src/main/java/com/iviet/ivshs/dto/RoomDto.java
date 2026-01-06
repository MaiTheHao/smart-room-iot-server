package com.iviet.ivshs.dto;

import lombok.Builder;

@Builder
public record RoomDto(
    Long id,
    String code,
    String name,
    String description,
    Long floorId
) {}