package com.iviet.ivshs.dto;

import java.time.Instant;

import lombok.Builder;

@Builder
public record AverageTemperatureValueDto(
    Instant timestamp,
    Double avgTempC
) {
    public AverageTemperatureValueDto(Long unixSeconds, Double avgTempC) {
        this(Instant.ofEpochSecond(unixSeconds), avgTempC);
    }
}