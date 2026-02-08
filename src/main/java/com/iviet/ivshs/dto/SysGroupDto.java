package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.SysGroup;
import com.iviet.ivshs.entities.SysGroupLan;
import lombok.Builder;

@Builder
public record SysGroupDto(
    Long id,
    String groupCode,
    String name,
    String description
) {
    public static SysGroupDto from(SysGroup entity, SysGroupLan groupLan) {
        return SysGroupDto.builder()
            .id(entity.getId())
            .groupCode(entity.getGroupCode())
            .name(groupLan.getName())
            .description(groupLan.getDescription())
            .build();
    }
}
