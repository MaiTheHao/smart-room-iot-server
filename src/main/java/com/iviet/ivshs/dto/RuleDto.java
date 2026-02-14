package com.iviet.ivshs.dto;

import java.time.Instant;
import java.util.List;

import com.iviet.ivshs.entities.Rule;
import lombok.Builder;

@Builder
public record RuleDto(
    Long id,
    String name,
    Integer priority,
    Boolean isActive,
    Long roomId,
    Long targetDeviceId,
    String targetDeviceCategory,
    String actionParams,
    List<RuleConditionDto> conditions,
    Instant createdAt,
    Instant updatedAt
) {
    public static RuleDto from(Rule entity) {
        if (entity == null) return null;
        return new RuleDto(
            entity.getId(),
            entity.getName(),
            entity.getPriority(),
            entity.getIsActive(),
            entity.getRoomId(),
            entity.getTargetDeviceId(),
            entity.getTargetDeviceCategory(),
            entity.getActionParams(),
            RuleConditionDto.fromEntities(entity.getConditions()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public static List<RuleDto> fromEntities(List<Rule> entities) {
        if (entities == null) return List.of();
        return entities.stream()
            .map(RuleDto::from)
            .toList();
    }
}
