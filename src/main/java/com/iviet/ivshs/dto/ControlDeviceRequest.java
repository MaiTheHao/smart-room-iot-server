package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.GatewayCommandV1;

import lombok.Builder;

@Builder
public record ControlDeviceRequest (
	// Long clientId,
	// Integer gpio,
	// DeviceControlTypeV1 controlType,
	// String clientIpAddress,
	// String targetNaturalId,
	GatewayCommandV1 command
){}