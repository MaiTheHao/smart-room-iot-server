package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.PowerConsumptionValue;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Builder
public record CreatePowerConsumptionValueDto(
    @NotNull(message = "Sensor natural ID is required")
    String sensorNaturalId,

    @NotNull(message = "Watt is required")
    Double watt,

    @NotNull(message = "Timestamp is required")
    Instant timestamp
) {
    public PowerConsumptionValue toEntity() {
        var entity = new PowerConsumptionValue();
        entity.setWatt(watt);
        entity.setTimestamp(timestamp);
        return entity;
    }
}