package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.enumeration.RuleDataSource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateRuleConditionDto(
    @NotNull(message = "Sort order is required")
    @Min(value = 0, message = "Sort order must be at least 0")
    Integer sortOrder,

    @NotNull(message = "Data source is required")
    RuleDataSource dataSource,

    @NotNull(message = "Resource parameters are required")
    JsonNode resourceParam,

    @NotBlank(message = "Operator is required")
    String operator,

    @NotBlank(message = "Value parameter is required")
    String value,

    String nextLogic
) {
}
