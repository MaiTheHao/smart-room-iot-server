package com.iviet.ivshs.dto;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.enumeration.DeviceCategory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateRuleDto(
    @NotBlank(message = "Rule name is required")
    String name,

    @NotNull(message = "Priority is required")
    @Min(value = 0, message = "Priority must be at least 0")
    Integer priority,

    @NotNull(message = "Target device ID is required")
    Long targetDeviceId,

    @NotNull(message = "Target device category is required")
    DeviceCategory targetDeviceCategory,

    @NotNull(message = "Action parameters are required")
    JsonNode actionParams,

    Boolean isActive,

    @Valid
    @NotNull(message = "At least one condition is required")
    List<UpdateRuleConditionDto> conditions
) {}
