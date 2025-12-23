package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.constant.HttpClientUrlConstant;
import com.iviet.ivshs.dto.ControlDeviceRequestV1;
import com.iviet.ivshs.dto.ControlDeviceResponseV1;
import com.iviet.ivshs.entities.DeviceControlV1;
import com.iviet.ivshs.enumeration.GatewayCommandV1;
import com.iviet.ivshs.service.ControlServiceV1;
import com.iviet.ivshs.util.HttpClientUtil;
import org.springframework.stereotype.Service;

@Service
public class ControlServiceImplV1 implements ControlServiceV1 {

	@Override
	public ControlDeviceResponseV1 controlDevice(ControlDeviceRequestV1 request) {
		if (request == null || request.getDeviceControlType() == null || request.getClientIpAddress() == null) {
			return ControlDeviceResponseV1.builder().status("400").error("Unknown Control Type").message(null).build();
		}

		String url = HttpClientUrlConstant.getBaseUrl(request.getClientIpAddress(), HttpClientUrlConstant.BASE_PATH_V1)
				+ HttpClientUrlConstant.CONTROL_ACTUATOR;

		try {
			HttpClientUtil.Response response = HttpClientUtil.post(url, request);
			return handleResponse(response);
		} catch (Exception e) {
			e.printStackTrace();
			return ControlDeviceResponseV1.builder().status("500").error("Internal Server Error").message(null).build();
		}
	}

	@Override
	public ControlDeviceResponseV1 turnOn(DeviceControlV1 deviceControl) {
		if (deviceControl == null) {
			return ControlDeviceResponseV1.builder().status("400").error("Device Control is required").message(null).build();
		}
		ControlDeviceRequestV1 request = buildRequest(deviceControl, GatewayCommandV1.ON.getValue());
		return controlDevice(request);
	}

	@Override
	public ControlDeviceResponseV1 turnOff(DeviceControlV1 deviceControl) {
		if (deviceControl == null) {
			return ControlDeviceResponseV1.builder().status("400").error("Device Control is required").message(null).build();
		}
		ControlDeviceRequestV1 request = buildRequest(deviceControl, GatewayCommandV1.OFF.getValue());
		return controlDevice(request);
	}

	@Override
	public ControlDeviceResponseV1 setLevel(DeviceControlV1 deviceControl, int level) {
		if (deviceControl == null) {
			return ControlDeviceResponseV1.builder().status("400").error("Device Control is required").message(null).build();
		}
		if (level < 0 || level > 100) {
			return ControlDeviceResponseV1.builder().status("400").error("Level must be between 0 and 100").message(null).build();
		}
		ControlDeviceRequestV1 request = buildRequest(deviceControl, GatewayCommandV1.levelCommand(level));
		return controlDevice(request);
	}

	@Override
	public ControlDeviceRequestV1 buildRequest(DeviceControlV1 deviceControl, String command) {
		if (deviceControl == null || deviceControl.getClient() == null) {
			return null;
		}
		return ControlDeviceRequestV1.builder()
				.deviceControlType(deviceControl.getDeviceControlType())
				.clientId(deviceControl.getClient().getId())
				.clientIpAddress(deviceControl.getClient().getIpAddress())
				.gpioPin(deviceControl.getGpioPin())
				.command(command)
				.build();
	}

	private ControlDeviceResponseV1 handleResponse(HttpClientUtil.Response response) {
		String statusCode = String.valueOf(response.getStatusCode());
		return switch (response.getStatusCode()) {
			case 200 -> ControlDeviceResponseV1.builder().status(statusCode).error(null).message("successful").build();
			case 400 -> {
				ControlDeviceResponseV1 errorResp = HttpClientUtil.fromJson(response.getBody(), ControlDeviceResponseV1.class);
				String errorMsg = errorResp != null ? errorResp.getError() : "Failed to control device";
				yield ControlDeviceResponseV1.builder().status(statusCode).error(errorMsg).message(null).build();
			}
			case 405 -> ControlDeviceResponseV1.builder().status(statusCode).error("The method not supported.").message(null).build();
			default -> ControlDeviceResponseV1.builder().status(statusCode).error("Internal Server Error").message(null).build();
		};
	}
}
