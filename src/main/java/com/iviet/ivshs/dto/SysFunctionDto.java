package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.SysFunction;
import com.iviet.ivshs.entities.SysFunctionLan;
import lombok.Builder;

@Builder
public record SysFunctionDto(
    Long id,
    String functionCode,
    String name,
    String description
) {
    public static SysFunctionDto from(SysFunction entity, SysFunctionLan functionLan) {
        return SysFunctionDto.builder()
            .id(entity.getId())
            .functionCode(entity.getFunctionCode())
            .name(functionLan.getName())
            .description(functionLan.getDescription())
            .build();
    }
}
