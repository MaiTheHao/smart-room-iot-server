package com.iviet.ivshs.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record UpdateLightDtoV1(
    @Size(min = 1, max = 100)
    String name,

    String naturalId,

    @Size(max = 255)
    String description,

    Boolean isActive,

    @Min(0) @Max(100)
    Integer level,

    Long roomId,
    Long deviceControlId,
    String langCode
) {}