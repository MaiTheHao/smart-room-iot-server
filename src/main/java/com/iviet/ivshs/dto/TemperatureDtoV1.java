package com.iviet.ivshs.dto;

import lombok.Builder;

@Builder
public record TemperatureDtoV1(
    Long id,
    String name,
    String description,
    Boolean isActive,
    Double currentValue,
    String naturalId,
    Long roomId
) {}