package com.iviet.ivshs.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateTemperatureValueDto(
    @NotBlank(message = "Sensor natural ID is required") String sensorNaturalId,
    @NotNull(message = "Temperature value (°C) is required") Double tempC,
    @NotNull(message = "Timestamp is required") Instant timestamp
) {}