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

    // AC Control Endpoints
    private static final String PATH_AC_POWER = "/ac/%s/power";
    private static final String PATH_AC_TEMP_UP = "/ac/%s/temp_up";
    private static final String PATH_AC_TEMP_DOWN = "/ac/%s/temp_down";
    private static final String PATH_AC_MODE = "/ac/%s/mode";
    private static final String PATH_AC_FAN = "/ac/%s/fan";
    private static final String PATH_AC_SWING = "/ac/%s/swing";

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

    // AC Control Methods
    public static String getAcPowerUrlV1(String ip, String naturalId) {
        return build(ip, BASE_PATH_V1, String.format(PATH_AC_POWER, naturalId));
    }

    public static String getAcTempUpUrlV1(String ip, String naturalId) {
        return build(ip, BASE_PATH_V1, String.format(PATH_AC_TEMP_UP, naturalId));
    }

    public static String getAcTempDownUrlV1(String ip, String naturalId) {
        return build(ip, BASE_PATH_V1, String.format(PATH_AC_TEMP_DOWN, naturalId));
    }

    public static String getAcModeUrlV1(String ip, String naturalId) {
        return build(ip, BASE_PATH_V1, String.format(PATH_AC_MODE, naturalId));
    }

    public static String getAcFanUrlV1(String ip, String naturalId) {
        return build(ip, BASE_PATH_V1, String.format(PATH_AC_FAN, naturalId));
    }

    public static String getAcSwingUrlV1(String ip, String naturalId) {
        return build(ip, BASE_PATH_V1, String.format(PATH_AC_SWING, naturalId));
    }

    // --- Utils ---

    private static String build(@NonNull String ip, @NonNull String basePath, @NonNull String endpoint) {
        String host = ip;
        Integer port = DEFAULT_PORT;

        try {
            java.net.URI uri = java.net.URI.create("http://" + ip);
            if (uri.getHost() != null) {
                host = uri.getHost();
            }
            if (uri.getPort() != -1) {
                port = uri.getPort();
            }
        } catch (Exception e) {
            if (ip.contains(":") && !ip.contains("]")) {
                int lastColon = ip.lastIndexOf(":");
                host = ip.substring(0, lastColon);
                port = Integer.valueOf(ip.substring(lastColon + 1));
            }
        }

        return UriComponentsBuilder.newInstance()
                .scheme(SCHEME)
                .host(host)
                .port(port)
                .path(basePath)
                .path(endpoint)
                .build()
                .toUriString();
    }
}