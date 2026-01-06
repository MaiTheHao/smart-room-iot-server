package com.iviet.ivshs.dto;

import lombok.Builder;

@Builder
public record FloorDto(
    Long id,
    String name,
    String code,
    String description,
    Integer level
) {
}