package com.iviet.ivshs.dto;

import lombok.Builder;

@Builder
public record SysFunctionDtoV1(
    Long id,
    String functionCode,
    String name,
    String description
) {
}
