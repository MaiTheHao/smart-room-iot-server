package com.iviet.ivshs.dto;

import com.iviet.ivshs.shared.enumeration.RoomEventCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateRoomEventConfigDto(
    @NotNull(message = "Event code cannot be null")
    RoomEventCode eventCode,

    Boolean isActive,

    @Min(value = 0, message = "Cooldown seconds must be greater than or equal to 0")
    Integer cooldownSeconds
) {}
