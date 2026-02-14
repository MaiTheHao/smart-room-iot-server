package com.iviet.ivshs.dto;

import java.time.Instant;
import java.util.List;

import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.enumeration.RuleDataSource;
import lombok.Builder;

@Builder
public record RuleConditionDto(
    Long id,
    Integer sortOrder,
    RuleDataSource dataSource,
    String resourceParam,
    String operator,
    String value,
    String nextLogic,
    Instant createdAt,
    Instant updatedAt
) {
    public static RuleConditionDto from(RuleCondition entity) {
        if (entity == null) return null;
        return new RuleConditionDto(
            entity.getId(),
            entity.getSortOrder(),
            entity.getDataSource(),
            entity.getResourceParam(),
            entity.getOperator(),
            entity.getValue(),
            entity.getNextLogic(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public static List<RuleConditionDto> fromEntities(List<RuleCondition> entities) {
        if (entities == null) return List.of();
        return entities.stream()
            .map(RuleConditionDto::from)
            .toList();
    }
}
