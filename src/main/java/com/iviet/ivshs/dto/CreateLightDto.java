package com.iviet.ivshs.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record CreateLightDto(
    @NotBlank(message = "Light name is required")
    @Size(min = 1, max = 100)
    String name,

    @NotBlank(message = "Natural ID is required")
    String naturalId,

    @Size(max = 255)
    String description,

    Boolean isActive,

    @Min(0) @Max(100)
    Integer level,

    @NotNull(message = "Room ID is required")
    Long roomId,

    Long deviceControlId,

    @Size(max = 10)
    String langCode
) {}