package com.iviet.ivshs.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Deprecated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UrlConstant {

    private static final int DEFAULT_PORT = 9090;
    private static final String SCHEME = "http";
    private static final String API_V1 = "/api/v1";
    private static final String API_V2 = "/api/v2";

    public static String getSetupUrlV1(String ip) {
        throwIfEmpty(ip, "IP address cannot be null or empty for setup API");
        return build(ip, API_V1, "setup");
    }

    public static String getHealthUrlV1(String ip) {
        throwIfEmpty(ip, "IP address cannot be null or empty for health check API");
        return build(ip, API_V1, "health-check");
    }

    public static String getTelemetryTempV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for telemetry temperature API");
        return build(ip, API_V1, "temperature/" + naturalId);
    }

    public static String getTelemetryPowerV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for telemetry power consumption API");
        return build(ip, API_V1, "power-consumption/" + naturalId);
    }

    public static String getTelemetryByGatewayV1(String ip) {
        throwIfEmpty(ip, "IP address cannot be null or empty for telemetry API");
        return build(ip, API_V1, "telemetry");
    }

    public static String getAcEnergyTelemetryV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for AC energy telemetry API");
        throwIfEmpty(naturalId, "Natural ID cannot be null or empty for AC energy telemetry API");
        return build(ip, API_V1, "air-conditions/" + naturalId + "/telemetry");
    }

    public static String getFanEnergyTelemetryV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for fan energy telemetry API");
        throwIfEmpty(naturalId, "Natural ID cannot be null or empty for fan energy telemetry API");
        return build(ip, API_V1, "fans/" + naturalId + "/telemetry");
    }

    public static String getLightEnergyTelemetryV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for light energy telemetry API");
        throwIfEmpty(naturalId, "Natural ID cannot be null or empty for light energy telemetry API");
        return build(ip, API_V1, "lights/" + naturalId + "/telemetry");
    }

    public static String getRoomEnergyTelemetryV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for room energy telemetry API");
        throwIfEmpty(naturalId, "Natural ID cannot be null or empty for room energy telemetry API");
        return build(ip, API_V1, "power-consumptions/" + naturalId + "/telemetry");
    }

    public static String getAcEnergyResetV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for AC energy reset API");
        throwIfEmpty(naturalId, "Natural ID cannot be null or empty for AC energy reset API");
        return build(ip, API_V1, "air-conditions/" + naturalId + "/reset");
    }

    public static String getFanEnergyResetV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for fan energy reset API");
        throwIfEmpty(naturalId, "Natural ID cannot be null or empty for fan energy reset API");
        return build(ip, API_V1, "fans/" + naturalId + "/reset");
    }

    public static String getLightEnergyResetV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for light energy reset API");
        throwIfEmpty(naturalId, "Natural ID cannot be null or empty for light energy reset API");
        return build(ip, API_V1, "lights/" + naturalId + "/reset");
    }

    public static String getRoomEnergyResetV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for room energy reset API");
        throwIfEmpty(naturalId, "Natural ID cannot be null or empty for room energy reset API");
        return build(ip, API_V1, "power-consumptions/" + naturalId + "/reset");
    }

    public static String getControlUrlV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for device control API");
        return build(ip, API_V1, "control/" + naturalId);
    }

    public static String getControlLightPowerUrlV2(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for light power control API");
        return build(ip, API_V2, "light/" + naturalId + "/power");
    }

    public static String getControlLightLevelUrlV2(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for light level control API");
        return build(ip, API_V2, "light/" + naturalId + "/level");
    }

    public static String getAcPowerUrlV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for AC power control API");
        return build(ip, API_V1, "ac/" + naturalId + "/power");
    }

    public static String getAcTempUpUrlV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for AC temperature up API");
        return build(ip, API_V1, "ac/" + naturalId + "/temp_up");
    }

    public static String getAcTempDownUrlV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for AC temperature down API");
        return build(ip, API_V1, "ac/" + naturalId + "/temp_down");
    }

    public static String getAcModeUrlV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for AC mode control API");
        return build(ip, API_V1, "ac/" + naturalId + "/mode");
    }

    public static String getAcFanUrlV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for AC fan control API");
        return build(ip, API_V1, "ac/" + naturalId + "/fan");
    }

    public static String getAcSwingUrlV1(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for AC swing control API");
        return build(ip, API_V1, "ac/" + naturalId + "/swing");
    }

    public static String getControlAcPowerUrlV2(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for AC power control API");
        return build(ip, API_V2, "ac/" + naturalId + "/power");
    }

    public static String getControlAcTempUpUrlV2(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for AC temperature up API");
        return build(ip, API_V2, "ac/" + naturalId + "/temp_up");
    }

    public static String getControlAcTempDownUrlV2(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for AC temperature down API");
        return build(ip, API_V2, "ac/" + naturalId + "/temp_down");
    }

    public static String getControlAcSwingUrlV2(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for AC swing control API");
        return build(ip, API_V2, "ac/" + naturalId + "/swing");
    }

    public static String getControlAcModeUrlV2(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for AC mode control API");
        return build(ip, API_V2, "ac/" + naturalId + "/mode");
    }

    public static String getControlAcFanUrlV2(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for AC fan control API");
        return build(ip, API_V2, "ac/" + naturalId + "/fan");
    }

    public static String getControlFanPowerUrlV2(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for fan power control API");
        return build(ip, API_V2, "fan/" + naturalId + "/power");
    }

    public static String getControlFanSpeedUrlV2(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for fan speed control API");
        return build(ip, API_V2, "fan/" + naturalId + "/speed");
    }

    public static String getControlFanModeUrlV2(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for fan mode control API");
        return build(ip, API_V2, "fan/" + naturalId + "/mode");
    }

    public static String getControlFanSwingUrlV2(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for fan swing control API");
        return build(ip, API_V2, "fan/" + naturalId + "/swing");
    }

    public static String getControlFanLightUrlV2(String ip, String naturalId) {
        throwIfEmpty(ip, "IP address cannot be null or empty for fan light control API");
        return build(ip, API_V2, "fan/" + naturalId + "/light");
    }

    private static void throwIfEmpty(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static String build(@NonNull String ip, @NonNull String basePath, @NonNull String endpoint) {
        String host = ip;
        int port = DEFAULT_PORT;

        try {
            URI uri = URI.create(SCHEME + "://" + ip);
            if (uri.getHost() != null) {
                host = uri.getHost();
            }
            if (uri.getPort() != -1) {
                port = uri.getPort();
            }
        } catch (IllegalArgumentException e) {
            int lastColon = ip.lastIndexOf(':');
            if (lastColon != -1 && !ip.endsWith("]")) {
                host = ip.substring(0, lastColon);
                try {
                    port = Integer.parseInt(ip.substring(lastColon + 1));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        String finalPath = basePath.endsWith("/") ? basePath + endpoint : basePath + "/" + endpoint;

        return UriComponentsBuilder.newInstance()
                .scheme(SCHEME)
                .host(host)
                .port(port)
                .path(finalPath)
                .build()
                .toUriString();
    }
}