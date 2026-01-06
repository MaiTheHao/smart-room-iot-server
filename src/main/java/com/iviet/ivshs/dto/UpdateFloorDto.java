package com.iviet.ivshs.dto;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Builder
public record UpdateFloorDto(
    @Size(min = 1, max = 100, message = "Floor name must be between 1 and 100 characters")
    String name,

    @NotBlank(message = "Floor code is required")
    @Size(max = 256, message = "Floor code must not exceed 256 characters")
    String code,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description,

    Integer level,

    @Size(max = 10, message = "Language code must not exceed 10 characters")
    String langCode
){}