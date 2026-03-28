package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Temperature;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Builder
public record CreateTemperatureDto(
    @NotBlank(message = "Temperature sensor name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    String name,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description,

    @NotBlank(message = "Natural ID is required")
    @Size(max = 100)
    String naturalId,

    Boolean isActive,

    @NotNull(message = "Room ID is required")
    Long roomId,

    @NotNull(message = "Device Control ID is required")
    Long deviceControlId,

    @Size(max = 10, message = "Language code must not exceed 10 characters")
    String langCode
) {
    public Temperature toEntity() {
        var entity = new Temperature();
        entity.setNaturalId(naturalId);
        entity.setIsActive(isActive != null ? isActive : true);
        return entity;
    }
}