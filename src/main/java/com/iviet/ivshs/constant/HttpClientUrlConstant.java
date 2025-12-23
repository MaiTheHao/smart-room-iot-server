package com.iviet.ivshs.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpClientUrlConstant {

	private static final String BASE_URL_TEMPLATE = "http://%s:8080%s";
	public static final String BASE_PATH_V1 = "/api/v1";
	
	public static String getBaseUrl(String ipAddress, String basePath) {
		String normalizedBasePath = basePath == null ? "" : basePath.trim();
		if (!normalizedBasePath.startsWith("/")) {
			normalizedBasePath = "/" + normalizedBasePath;
		}
		if (normalizedBasePath.length() > 1 && normalizedBasePath.endsWith("/")) {
			normalizedBasePath = normalizedBasePath.substring(0, normalizedBasePath.length() - 1);
		}
		return String.format(BASE_URL_TEMPLATE, ipAddress, normalizedBasePath);
	}
	
	public static final String TEST_CONNECTION = "/test";
	public static final String HEALTH_CHECK = "/health-check/";
	public static final String CONTROL_ACTUATOR = "/control/";
	public static final String READ_TEMPERATURE = "/temperature/{id}";
	public static final String READ_POWER_CONSUMPTION = "/power-consumption/{id}";
}
