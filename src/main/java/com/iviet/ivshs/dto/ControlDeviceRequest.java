package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.GatewayCommand;

import lombok.Builder;

@Builder
public record ControlDeviceRequest (
	// Long clientId,
	// Integer gpio,
	// DeviceControlTypeV1 controlType,
	// String clientIpAddress,
	// String targetNaturalId,
	GatewayCommand command
){}