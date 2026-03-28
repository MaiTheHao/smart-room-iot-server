package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.SysGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateSysGroupDto(
    @NotBlank(message = "Group code is required")
    @Size(max = 100, message = "Group code must not exceed 100 characters")
    String groupCode,

    @NotBlank(message = "Group name is required")
    @Size(min = 1, max = 100, message = "Group name must be between 1 and 100 characters")
    String name,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description,

    @Size(max = 10, message = "Language code must not exceed 10 characters")
    String langCode
) {
    public SysGroup toEntity() {
        var group = new SysGroup();
        group.setGroupCode(groupCode);
        return group;
    }
}
