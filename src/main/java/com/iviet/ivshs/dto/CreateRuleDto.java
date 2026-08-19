package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Rule;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateRuleDto(
    @NotBlank(message = "Rule name cannot be blank") String name,
    @NotNull(message = "Priority cannot be null") Integer priority,

    @NotNull(message = "Interval seconds cannot be null")
    @Min(value = 60, message = "Interval seconds must be at least 60")
    Integer intervalSeconds) {

  public Rule toEntity() {
    Rule entity = new Rule();
    entity.setName(name);
    entity.setPriority(priority != null ? priority : 0);
    entity.setIntervalSeconds(intervalSeconds);
    entity.setIsInterval(intervalSeconds != null && intervalSeconds > 0);
    entity.setIsActive(true);
    return entity;
  }
}
