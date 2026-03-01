package com.iviet.ivshs.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateRuleStatusDto(
        @NotNull(message = "isActive is required")
        Boolean isActive
) {
}
