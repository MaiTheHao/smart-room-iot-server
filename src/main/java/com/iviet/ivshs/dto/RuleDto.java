package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Rule;
import java.time.Instant;
import lombok.Builder;

@Builder
public record RuleDto(
    Long id,
    String name,
    Integer priority,
    Boolean isActive,
    Integer intervalSeconds,
    Instant createdAt,
    Instant updatedAt) {

  public static RuleDto fromEntity(Rule entity) {
    if (entity == null) {
      return null;
    }
    return RuleDto.builder()
        .id(entity.getId())
        .name(entity.getName())
        .priority(entity.getPriority())
        .isActive(entity.getIsActive())
        .intervalSeconds(entity.getIntervalSeconds())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  public Rule toEntity() {
    Rule entity = new Rule();
    entity.setId(id);
    entity.setName(name);
    entity.setPriority(priority != null ? priority : 0);
    entity.setIsActive(isActive);
    entity.setIntervalSeconds(intervalSeconds);
    entity.setIsInterval(intervalSeconds != null && intervalSeconds > 0);
    return entity;
  }
}
