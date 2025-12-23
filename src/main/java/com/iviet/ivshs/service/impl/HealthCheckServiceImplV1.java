package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.constant.HttpClientUrlConstant;
import com.iviet.ivshs.dto.HealthCheckRequestDtoV1;
import com.iviet.ivshs.dto.HealthCheckResponseDtoV1;
import com.iviet.ivshs.exception.BadRequestException;
import com.iviet.ivshs.service.HealthCheckServiceV1;
import com.iviet.ivshs.util.HttpClientUtil;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckServiceImplV1 implements HealthCheckServiceV1 {

	@Override
	public HealthCheckResponseDtoV1 check(HealthCheckRequestDtoV1 request) {
		if (request == null || request.getDeviceControlType() == null || request.getClientIpAddress() == null) {
			throw new BadRequestException("Missing required fields in HealthCheckRequestDtoV1");
		}

		String url = HttpClientUrlConstant.getBaseUrl(request.getClientIpAddress(), HttpClientUrlConstant.BASE_PATH_V1)
				+ HttpClientUrlConstant.HEALTH_CHECK;

		try {
			HttpClientUtil.Response response = HttpClientUtil.post(url, request);
			return handleResponse(response);
		} catch (Exception e) {
			return HealthCheckResponseDtoV1.builder().status("500").active(false).build();
		}
	}

	private HealthCheckResponseDtoV1 handleResponse(HttpClientUtil.Response response) {
		String statusCode = String.valueOf(response.getStatusCode());
		return switch (response.getStatusCode()) {
			case 200 -> {
				HealthCheckResponseDtoV1 resp = HttpClientUtil.fromJson(response.getBody(), HealthCheckResponseDtoV1.class);
				if (resp != null) {
					resp.setStatus(statusCode);
					yield resp;
				}
				yield HealthCheckResponseDtoV1.builder().status(statusCode).active(true).build();
			}
			case 400, 405 -> HealthCheckResponseDtoV1.builder().status(statusCode).active(false).build();
			default -> HealthCheckResponseDtoV1.builder().status(statusCode).active(false).build();
		};
	}
}
