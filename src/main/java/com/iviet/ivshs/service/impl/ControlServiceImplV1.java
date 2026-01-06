package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dto.ControlDeviceRequest;
import com.iviet.ivshs.dto.ControlDeviceResponse;
import com.iviet.ivshs.enumeration.GatewayCommandV1;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.ExternalServiceException;
import com.iviet.ivshs.exception.domain.NetworkTimeoutException;
import com.iviet.ivshs.service.ControlServiceV1;
import com.iviet.ivshs.util.HttpClientUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ControlServiceImplV1 implements ControlServiceV1 {
	
	@Override
	public ControlDeviceResponse sendCommand(String gatewayIp, String targetNaturalId, GatewayCommandV1 command) {
		if (gatewayIp == null || gatewayIp.isEmpty()) throw new BadRequestException("Gateway IP is required");
		
		if (targetNaturalId == null || targetNaturalId.isEmpty()) throw new BadRequestException("Target Natural ID is required");
		
		if (command == null) throw new BadRequestException("Command is required");

		long start = System.currentTimeMillis();
		String url = UrlConstant.getControlUrlV1(gatewayIp, targetNaturalId);
		ControlDeviceRequest requestBody = ControlDeviceRequest.builder()
				.command(command)
				.build();
		
		try {
			log.info("[CONTROL] Starting command [{}] to device [{}] at IP [{}]", command, targetNaturalId, gatewayIp);
			
			HttpClientUtil.Response response = HttpClientUtil.post(url, requestBody);

			if (!response.isSuccess()) {
				log.warn("[CONTROL] Device rejected command. Status: {}, Body: {}", 
						response.getStatusCode(), response.getBody());
				throw new ExternalServiceException("Device rejected command (Error code: " + response.getStatusCode() + ")");
			}

			String body = response.getBody();
			if (body == null || body.trim().isEmpty() || !body.trim().startsWith("{")) {
				log.error("[CONTROL] Device sent corrupted data: {}", body);
				throw new ExternalServiceException("Device returned invalid data.");
			}

			ControlDeviceResponse result = HttpClientUtil.fromJson(body, ControlDeviceResponse.class);
			log.info("[CONTROL] Finished command [{}] to device [{}] at IP [{}] in {}ms", command, targetNaturalId, gatewayIp, System.currentTimeMillis() - start);
			return result;

		} catch (NetworkTimeoutException e) {
			log.error("[CONTROL] Target device {} is unreachable at IP {}: {}", targetNaturalId, gatewayIp, e.getMessage());
			throw new NetworkTimeoutException("Cannot connect to device. Please check power or network.");
		} catch (ExternalServiceException e) {
			log.error("[CONTROL] External service error: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("[CONTROL] Unexpected error: {}", e.getMessage());
			throw new ExternalServiceException("System error when controlling device: " + e.getMessage());
		}
	}
}