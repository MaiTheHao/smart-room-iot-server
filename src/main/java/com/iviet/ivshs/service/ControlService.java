package com.iviet.ivshs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dto.ControlDeviceResponse;
import com.iviet.ivshs.enumeration.GatewayCommand;

public interface ControlService {
	ControlDeviceResponse sendCommand(String gatewayIp, String targetNaturalId, GatewayCommand command);
	ControlDeviceResponse sendCommand(String gatewayAddress, String targetNaturalId, JsonNode command);
}