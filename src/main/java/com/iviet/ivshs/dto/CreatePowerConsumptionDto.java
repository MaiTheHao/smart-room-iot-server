package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.PowerConsumption;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Builder
public record CreatePowerConsumptionDto(
    @NotBlank(message = "Power consumption sensor name is required")
    @Size(min = 1, max = 100, message = "Power consumption sensor name must be between 1 and 100 characters")
    String name,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description,

    Boolean isActive,

    @Size(max = 100, message = "Natural ID must not exceed 100 characters")
    String naturalId,

    @Size(max = 10, message = "Language code must not exceed 10 characters")
    String langCode,

    @NotNull(message = "Room ID is required")
    Long roomId,

    @NotNull(message = "Device Control ID is required")
    Long deviceControlId
) {
    public PowerConsumption toEntity() {
        var entity = new PowerConsumption();
        entity.setNaturalId(naturalId);
        entity.setIsActive(isActive != null ? isActive : true);
        return entity;
    }
}
