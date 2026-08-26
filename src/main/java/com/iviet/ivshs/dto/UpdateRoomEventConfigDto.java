package com.iviet.ivshs.dto;

import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
public record UpdateRoomEventConfigDto(
    Boolean isActive,

    @Min(value = 0, message = "Cooldown seconds must be greater than or equal to 0")
    Integer cooldownSeconds
) {}
