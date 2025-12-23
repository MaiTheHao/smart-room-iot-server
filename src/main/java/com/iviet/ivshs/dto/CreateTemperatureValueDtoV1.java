package com.iviet.ivshs.dto;

import java.time.Instant;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateTemperatureValueDtoV1(
    @NotNull(message = "Temperature value (°C) is required")
    Double tempC,

    @NotNull(message = "Timestamp is required")
    Instant timestamp
) {}