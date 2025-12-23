package com.iviet.ivshs.dto;

import lombok.Builder;
import java.time.Instant;

@Builder
public record TemperatureValueDtoV1(
    Long id,
    Long sensorId,
    Double tempC,
    Instant timestamp
) {}