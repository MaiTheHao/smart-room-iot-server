package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.enumeration.ConditionLogic;
import com.iviet.ivshs.enumeration.ConditionOperator;
import com.iviet.ivshs.enumeration.RuleDataSource;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
public record UpdateRuleConditionV2Dto(
	Long id,

	@Min(value = 0, message = "Sort order must be at least 0")
	Integer sortOrder,

	RuleDataSource dataSource,

	JsonNode resourceParam,

	ConditionOperator operator,

	String valueParam,

	ConditionLogic nextLogic
) {}