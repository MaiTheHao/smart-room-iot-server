package com.iviet.ivshs.dto;

import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.enumeration.RuleDataSource;
import lombok.Builder;

@Builder
public record RuleConditionDto(
    Long id,
    Integer sortOrder,
    RuleDataSource dataSource,
    JsonNode resourceParam,
    String operator,
    String value,
    String nextLogic,
    Instant createdAt,
    Instant updatedAt
) {}
