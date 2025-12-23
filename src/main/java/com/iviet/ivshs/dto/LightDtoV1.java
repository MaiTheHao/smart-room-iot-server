package com.iviet.ivshs.dto;

import lombok.Builder;

@Builder
public record LightDtoV1(
    Long id,
    String naturalId,
    String name,
    String description,
    Boolean isActive,
    Integer level,
    Long roomId
) {}