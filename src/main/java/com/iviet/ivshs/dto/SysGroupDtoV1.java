package com.iviet.ivshs.dto;

import lombok.Builder;

@Builder
public record SysGroupDtoV1(
    Long id,
    String groupCode,
    String name,
    String description
) {
}
