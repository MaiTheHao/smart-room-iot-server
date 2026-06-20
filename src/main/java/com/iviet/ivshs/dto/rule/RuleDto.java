package com.iviet.ivshs.dto.rule;

import java.time.Instant;
import java.util.List;
import com.iviet.ivshs.dto.alert.RuleActionAlertDto;
import lombok.Builder;

@Builder
public record RuleDto(
		Long id,
		String name,
		Integer priority,
		Boolean isActive,
		Integer intervalSeconds,
		List<RuleConditionDto> conditions,
		List<RuleActionDto> actions,
		List<RuleActionAlertDto> alertConfigs,
		Instant createdAt,
		Instant updatedAt) {
}
