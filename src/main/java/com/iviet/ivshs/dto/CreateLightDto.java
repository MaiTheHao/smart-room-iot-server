package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Light;
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
) {
    public Light toEntity() {
        var light = new Light();
        light.setNaturalId(this.naturalId);
        light.setIsActive(this.isActive != null ? this.isActive : false);
        light.setLevel(this.level != null ? this.level : 0);
        return light;
    }
}