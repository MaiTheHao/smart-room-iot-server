package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dto.ControlDeviceRequest;
import com.iviet.ivshs.dto.ControlDeviceResponse;
import com.iviet.ivshs.enumeration.GatewayCommand;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.ControlService;
import com.iviet.ivshs.util.HttpClientUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ControlServiceImpl implements ControlService {
	
	@Override
	public ControlDeviceResponse sendCommand(String gatewayIp, String targetNaturalId, GatewayCommand command) {
		if (gatewayIp == null || gatewayIp.isEmpty()) throw new BadRequestException("Gateway IP is required");
		
		if (targetNaturalId == null || targetNaturalId.isEmpty()) throw new BadRequestException("Target Natural ID is required");
		
		if (command == null) throw new BadRequestException("Command is required");

		String url = UrlConstant.getControlUrlV1(gatewayIp, targetNaturalId);
		ControlDeviceRequest requestBody = ControlDeviceRequest.builder()
				.command(command)
				.build();
		
		log.info("[CONTROL] Sending command [{}] to device [{}] at IP [{}]", command, targetNaturalId, gatewayIp);
		
		HttpClientUtil.postAsync(url, requestBody)
			.exceptionally(ex -> null);
		
		return ControlDeviceResponse.builder().build();
	}
}
