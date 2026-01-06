package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.ControlDeviceResponse;
import com.iviet.ivshs.enumeration.GatewayCommandV1;

public interface ControlServiceV1 {
	ControlDeviceResponse sendCommand(String gatewayIp, String targetNaturalId, GatewayCommandV1 command);
}
