package com.iviet.ivshs.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
public record UpdateRuleV2Dto(
	String name,
	
	Integer priority,
	
	Boolean isActive,
	
	@Min(value = 60, message = "Interval seconds must be at least 60")
	Integer intervalSeconds,

	@Valid
	List<UpdateRuleConditionV2Dto> conditions,
	
	@Valid
	List<UpdateRuleActionV2Dto> actions
) {}
