package com.iviet.ivshs.dto;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.enumeration.DeviceCategory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateRuleDto(
    @NotBlank(message = "Rule name is required")
    String name,

    @NotNull(message = "Priority is required")
    Integer priority,

    @NotNull(message = "Room ID is required")
    Long roomId,

    @NotNull(message = "Target device ID is required")
    Long targetDeviceId,

    @NotNull(message = "Target device category is required")
    DeviceCategory targetDeviceCategory,

    @NotNull(message = "Action parameters are required")
    JsonNode actionParams,

    @Valid
    List<CreateRuleConditionDto> conditions
) {}