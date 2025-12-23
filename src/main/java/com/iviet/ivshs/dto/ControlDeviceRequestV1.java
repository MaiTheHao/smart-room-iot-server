package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.DeviceControlTypeV1;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControlDeviceRequestV1 {
	private DeviceControlTypeV1 deviceControlType;
	private Long clientId;
	private String clientIpAddress;
	private Integer gpioPin;
	private String command;
}
