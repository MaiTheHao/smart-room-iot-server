package com.iviet.ivshs.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateTemperatureDtoV1(
    @Size(min = 1, max = 100)
    String name,

    @Size(max = 255)
    String description,

    Boolean isActive,

    @Size(max = 10)
    String langCode
) {}