package com.iviet.ivshs.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.web.util.UriComponentsBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UrlConstant {

    private static final int DEFAULT_PORT = 8080;
    private static final String SCHEME = "http";
    public static final String BASE_PATH_V1 = "/api/v1";

    // --- Endpoints ---
    private static final String PATH_SETUP = "/setup";
    private static final String PATH_HEALTH = "/health-check";
    private static final String PATH_CONTROL = "/control/%s";
    private static final String PATH_TEMP = "/temperature/%s";
    private static final String PATH_POWER = "/power-consumption/%s";
    private static final String PATH_BATCH_TELEMETRY = "/telemetry";

    // --- Public API Methods ---

    public static String getSetupUrlV1(String ip) {
        return build(ip, BASE_PATH_V1, PATH_SETUP);
    }

    public static String getHealthUrlV1(String ip) {
        return build(ip, BASE_PATH_V1, PATH_HEALTH);
    }

    public static String getTelemetryTempV1(String ip, String naturalId) {
        return build(ip, BASE_PATH_V1, String.format(PATH_TEMP, naturalId));
    }

    public static String getTelemetryPowerV1(String ip, String naturalId) {
        return build(ip, BASE_PATH_V1, String.format(PATH_POWER, naturalId));
    }

    public static String getTelemetryByGatewayV1(String ip) {
        return build(ip, BASE_PATH_V1, PATH_BATCH_TELEMETRY);
    }

    public static String getControlUrlV1(String ip, String naturalId) {
        return build(ip, BASE_PATH_V1, String.format(PATH_CONTROL, naturalId));
    }

    // --- Utils ---

    private static String build(@NonNull String ip, @NonNull String basePath, @NonNull String endpoint) {
        return UriComponentsBuilder.newInstance()
                .scheme(SCHEME)
                .host(ip)
                .port(DEFAULT_PORT)
                .path(basePath)
                .path(endpoint)
                .build()
                .toUriString();
    }
}