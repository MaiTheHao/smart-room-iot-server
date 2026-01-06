package com.iviet.ivshs.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreatePowerConsumptionValueDto(
    @NotBlank(message = "Sensor natural ID is required") String sensorNaturalId,
    @NotNull(message = "Watt value is required") Double watt,
    @NotNull(message = "Timestamp is required") Instant timestamp
){}