package com.iviet.ivshs.dto;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.enumeration.DeviceCategory;

import jakarta.validation.Valid;
import lombok.Builder;

@Builder
public record UpdateRuleDto(
    String name,

    Integer priority,

    Long targetDeviceId,

    DeviceCategory targetDeviceCategory,

    JsonNode actionParams,

    Boolean isActive,

    @Valid
    List<UpdateRuleConditionDto> conditions
) {}
