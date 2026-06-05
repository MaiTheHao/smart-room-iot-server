package com.iviet.ivshs.dto.rule;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateRuleActionDto(
		@NotNull(message = "Target device ID cannot be null") Long targetDeviceId,

		@NotNull(message = "Device category cannot be null") DeviceCategory targetDeviceCategory,

		@NotNull(message = "Action parameters cannot be null") JsonNode actionParams,

		@NotNull(message = "Execution order cannot be null") Integer executionOrder) {
}
