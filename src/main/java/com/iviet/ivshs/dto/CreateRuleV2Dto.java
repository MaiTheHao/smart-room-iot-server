package com.iviet.ivshs.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateRuleV2Dto(
	@NotBlank(message = "Rule name cannot be blank")
	String name,
	
	@NotNull(message = "Priority cannot be null")
	Integer priority,
	
	@NotNull(message = "Room ID cannot be null")
	Long roomId,
	
	@NotNull(message = "Interval seconds cannot be null")
	@Min(value = 60, message = "Interval seconds must be at least 60")
	Integer intervalSeconds,
	
	@NotNull(message = "Conditions cannot be null")
	@Valid
	List<CreateRuleConditionV2Dto> conditions,
	
	@NotNull(message = "Actions cannot be null")
	@Valid
	List<CreateRuleActionV2Dto> actions
) {}
