package com.iviet.ivshs.dto;

import lombok.Builder;

@Builder
public record SysFunctionDto(
    Long id,
    String functionCode,
    String name,
    String description
) {
}
