package com.iviet.ivshs.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
public record UpdateRuleDto(
	String name,
	
	Integer priority,
	
	Boolean isActive,
	
	@Min(value = 60, message = "Interval seconds must be at least 60")
	Integer intervalSeconds,

	@Valid
	List<UpdateRuleConditionDto> conditions,
	
	@Valid
	List<UpdateRuleActionDto> actions
) {}
