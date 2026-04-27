package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dto.ControlDeviceResponse;
import com.iviet.ivshs.enumeration.GatewayCommand;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.ControlService;

import org.springframework.stereotype.Service;

@Service
@lombok.RequiredArgsConstructor
public class ControlServiceImpl implements ControlService {

	private final com.iviet.ivshs.service.client.gateway.GatewayControlClient controlClient;
	public ControlDeviceResponse sendCommand(String gatewayIp, String targetNaturalId, GatewayCommand command) {
		if (gatewayIp == null || gatewayIp.isEmpty()) throw new BadRequestException("Gateway IP is required");
		
		if (targetNaturalId == null || targetNaturalId.isEmpty()) throw new BadRequestException("Target Natural ID is required");
		
		if (command == null) throw new BadRequestException("Command is required");

		java.util.concurrent.CompletableFuture.runAsync(() -> {
			try {
				controlClient.controlDeviceV1(gatewayIp, targetNaturalId, command);
			} catch (Exception ignored) {}
		});
		
		return ControlDeviceResponse.builder().build();
	}

	@Override
	public ControlDeviceResponse sendCommand(String gatewayAddress, String targetNaturalId, JsonNode command) {
		throw new UnsupportedOperationException();
	}
}
