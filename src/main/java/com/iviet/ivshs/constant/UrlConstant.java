package com.iviet.ivshs.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.web.util.UriComponentsBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UrlConstant {

    private static final int DEFAULT_PORT = 8080;
    private static final String SCHEME = "http";

    public static String getSetupUrlV1(String ip) {
        return build(ip, "/api/v1", "setup");
    }

    public static String getHealthUrlV1(String ip) {
        return build(ip, "/api/v1", "health-check");
    }

    public static String getTelemetryTempV1(String ip, String naturalId) {
        return build(ip, "/api/v1", "temperature/" + naturalId);
    }

    public static String getTelemetryPowerV1(String ip, String naturalId) {
        return build(ip, "/api/v1", "power-consumption/" + naturalId);
    }

    public static String getTelemetryByGatewayV1(String ip) {
        return build(ip, "/api/v1", "telemetry");
    }

    public static String getControlUrlV1(String ip, String naturalId) {
        return build(ip, "/api/v1", "control/" + naturalId);
    }

    // Light Control Methods (V2)
    public static String getControlLightPowerUrlV2(String ip, String naturalId) {
        return build(ip, "/api/v2", "light/" + naturalId + "/power");
    }

    public static String getControlLightLevelUrlV2(String ip, String naturalId) {
        return build(ip, "/api/v2", "light/" + naturalId + "/level");
    }

    // AC Control Methods (V1)
    public static String getAcPowerUrlV1(String ip, String naturalId) {
        return build(ip, "/api/v1", "ac/" + naturalId + "/power");
    }

    public static String getAcTempUpUrlV1(String ip, String naturalId) {
        return build(ip, "/api/v1", "ac/" + naturalId + "/temp_up");
    }

    public static String getAcTempDownUrlV1(String ip, String naturalId) {
        return build(ip, "/api/v1", "ac/" + naturalId + "/temp_down");
    }

    public static String getAcModeUrlV1(String ip, String naturalId) {
        return build(ip, "/api/v1", "ac/" + naturalId + "/mode");
    }

    public static String getAcFanUrlV1(String ip, String naturalId) {
        return build(ip, "/api/v1", "ac/" + naturalId + "/fan");
    }

    public static String getAcSwingUrlV1(String ip, String naturalId) {
        return build(ip, "/api/v1", "ac/" + naturalId + "/swing");
    }

    // AC Control Methods (V2)
    public static String getControlAcPowerUrlV2(String ip, String naturalId) {
        return build(ip, "/api/v2", "air-condition/" + naturalId + "/power");
    }

    public static String getControlAcTempUpUrlV2(String ip, String naturalId) {
        return build(ip, "/api/v2", "air-condition/" + naturalId + "/temp_up");
    }

    public static String getControlAcTempDownUrlV2(String ip, String naturalId) {
        return build(ip, "/api/v2", "air-condition/" + naturalId + "/temp_down");
    }

    public static String getControlAcSwingUrlV2(String ip, String naturalId) {
        return build(ip, "/api/v2", "air-condition/" + naturalId + "/swing");
    }

    public static String getControlAcModeUrlV2(String ip, String naturalId) {
        return build(ip, "/api/v2", "air-condition/" + naturalId + "/mode");
    }

    public static String getControlAcFanUrlV2(String ip, String naturalId) {
        return build(ip, "/api/v2", "air-condition/" + naturalId + "/fan");
    }

    // Fan Control Methods (V2)
    public static String getControlFanPowerUrlV2(String ip, String naturalId) {
        return build(ip, "/api/v2", "fan/" + naturalId + "/power");
    }

    public static String getControlFanSpeedUrlV2(String ip, String naturalId) {
        return build(ip, "/api/v2", "fan/" + naturalId + "/speed");
    }

    public static String getControlFanModeUrlV2(String ip, String naturalId) {
        return build(ip, "/api/v2", "fan/" + naturalId + "/mode");
    }

    public static String getControlFanSwingUrlV2(String ip, String naturalId) {
        return build(ip, "/api/v2", "fan/" + naturalId + "/swing");
    }

    public static String getControlFanLightUrlV2(String ip, String naturalId) {
        return build(ip, "/api/v2", "fan/" + naturalId + "/light");
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

        String finalPath = (basePath.endsWith("/") ? basePath : basePath + "/") + endpoint;

        return UriComponentsBuilder.newInstance()
                .scheme(SCHEME)
                .host(host)
                .port(port)
                .path(finalPath)
                .build()
                .toUriString();
    }
}