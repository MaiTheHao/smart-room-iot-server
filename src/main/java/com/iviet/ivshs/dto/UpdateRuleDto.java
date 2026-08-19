package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Rule;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
public record UpdateRuleDto(
    String name,
    Integer priority,
    Boolean isActive,

    @Min(value = 60, message = "Interval seconds must be at least 60")
    Integer intervalSeconds) {

  public void updateEntity(Rule entity) {
    if (entity == null) {
      return;
    }
    if (name != null) {
      entity.setName(name);
    }
    if (priority != null) {
      entity.setPriority(priority);
    }
    if (isActive != null) {
      entity.setIsActive(isActive);
    }
    if (intervalSeconds != null) {
      entity.setIntervalSeconds(intervalSeconds);
      entity.setIsInterval(intervalSeconds > 0);
    }
  }
}
