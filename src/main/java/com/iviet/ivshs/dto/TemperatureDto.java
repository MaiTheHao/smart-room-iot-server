package com.iviet.ivshs.dto;

import lombok.Builder;

@Builder
public record TemperatureDto(
    Long id,
    String name,
    String description,
    Boolean isActive,
    Double currentValue,
    String naturalId,
    Long roomId
) {}