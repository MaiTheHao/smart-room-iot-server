package com.iviet.ivshs.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.enumeration.DeviceCategory;

import lombok.Builder;

@Builder
public record RuleDto(
    Long id,
    String name,
    Integer priority,
    Boolean isActive,
    Long roomId,
    Long targetDeviceId,
    DeviceCategory targetDeviceCategory,
    JsonNode actionParams,
    List<RuleConditionDto> conditions,
    Instant createdAt,
    Instant updatedAt
) {}
