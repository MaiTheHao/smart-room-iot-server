package com.iviet.ivshs.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record RuleV2Dto(
	Long id,
	String name,
	Integer priority,
	Boolean isActive,
	Long roomId,
	Integer intervalSeconds,
	List<RuleConditionV2Dto> conditions,
	List<RuleActionV2Dto> actions,
	Instant createdAt,
	Instant updatedAt
) {}
