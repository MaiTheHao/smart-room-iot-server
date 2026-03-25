package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.enumeration.DeviceCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateRuleActionV2Dto(
	@NotNull(message = "Target device ID cannot be null")
	Long targetDeviceId,
	
	@NotNull(message = "Device category cannot be null")
	DeviceCategory targetDeviceCategory,
	
	@NotNull(message = "Action parameters cannot be null")
	JsonNode actionParams,
	
	@NotNull(message = "Execution order cannot be null")
	Integer executionOrder
) {}
