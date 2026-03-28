package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.TemperatureValue;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Builder
public record CreateTemperatureValueDto(
    @NotNull(message = "Sensor natural ID is required")
    String sensorNaturalId,

    @NotNull(message = "Temperature in Celsius is required")
    Double tempC,

    @NotNull(message = "Timestamp is required")
    Instant timestamp
) {
    public TemperatureValue toEntity() {
        var entity = new TemperatureValue();
        entity.setTempC(tempC);
        entity.setTimestamp(timestamp);
        return entity;
    }
}