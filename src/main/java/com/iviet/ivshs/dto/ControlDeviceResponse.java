package com.iviet.ivshs.dto;

import java.time.Instant;

import lombok.Builder;

@Builder
public record ControlDeviceResponse (
	Integer status,
	String message,
	String data,
	Instant timestamp 
){}