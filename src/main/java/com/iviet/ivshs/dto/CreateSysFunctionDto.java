package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.SysFunction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateSysFunctionDto(
    @NotBlank(message = "Function code is required")
    @Size(max = 256, message = "Function code must not exceed 256 characters")
    String functionCode,

    @NotBlank(message = "Function name is required")
    @Size(min = 1, max = 100, message = "Function name must be between 1 and 100 characters")
    String name,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description,

    @Size(max = 10, message = "Language code must not exceed 10 characters")
    String langCode
) {
    public SysFunction toEntity() {
        var function = new SysFunction();
        function.setFunctionCode(functionCode);
        return function;
    }
}
