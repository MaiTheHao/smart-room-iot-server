package com.iviet.ivshs.dto;

import lombok.Builder;

@Builder
public record SysGroupWithClientStatusDto(
    Long id,
    String groupCode,
    String name,
    String description,
    Boolean isAssignedToClient
) {
}
