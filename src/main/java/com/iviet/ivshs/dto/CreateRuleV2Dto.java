package com.iviet.ivshs.dto;

import java.util.List;

import jakarta.validation.Valid;
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
	
	String cronExpression,
	
	@NotNull(message = "Conditions cannot be null")
	@Valid
	List<CreateRuleConditionV2Dto> conditions,
	
	@NotNull(message = "Actions cannot be null")
	@Valid
	List<CreateRuleActionV2Dto> actions
) {}
