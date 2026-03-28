package com.iviet.ivshs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import java.time.Instant;

@Builder
public record IngestTemperatureValueDto(
    @NotNull(message = "tempC must not be null")
    Double tempC,
    
    @NotNull(message = "timestamp must not be null")
    Instant timestamp,

    @NotNull(message = "gpio must not be null")
    Integer gpio
) {}