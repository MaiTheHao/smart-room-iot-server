package com.iviet.ivshs.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateRoomDtoV1(
    @Size(min = 1, max = 100, message = "Room name must be between 1 and 100 characters")
    String name,

    @Size(max = 256)
    String code,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description,

    Long floorId,

    @Size(max = 10, message = "Language code must not exceed 10 characters")
    String langCode
) {}