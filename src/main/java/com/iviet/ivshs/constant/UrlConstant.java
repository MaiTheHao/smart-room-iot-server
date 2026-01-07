package com.iviet.ivshs.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Định nghĩa và xây dựng các URL Endpoint cho hệ thống Smart Room IoT.
 * Cung cấp các phương thức để tạo URL chuẩn hóa từ IP và các Path tương ứng.
 * @version 1.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UrlConstant {

    /** Cổng mặc định của dịch vụ */
    private static final int DEFAULT_PORT = 8080;
    
    /** Giao thức mặc định */
    private static final String SCHEME = "http";

    // --- Base Paths ---
    /** Phiên bản API v1 */
    public static final String BASE_PATH_V1 = "/api/v1";

    // --- Endpoints ---
    private static final String PATH_SETUP = "/setup";
    private static final String PATH_HEALTH = "/health-check";
    private static final String PATH_CONTROL = "/control/%s";
    private static final String PATH_TEMP = "/temperature/%s";
    private static final String PATH_POWER = "/power-consumption/%s";
    private static final String PATH_BATCH_TELEMETRY = "/telemetry";

    // --- Public API Methods ---

    /**
     * Lấy URL lấy thông tin setup từ client IP.
     * * @param ip Địa chỉ IP của thiết bị/gateway
     * @return URL setup (e.g., http://ip:8080/api/v1/setup)
     */
    public static String getSetupUrlV1(String ip) {
        return build(ip, BASE_PATH_V1, PATH_SETUP);
    }

    /**
     * Lấy URL health check theo client IP.
     * * @param ip Địa chỉ IP của thiết bị/gateway
     * @return URL health check (e.g., http://ip:8080/api/v1/health-check)
     */
    public static String getHealthUrlV1(String ip) {
        return build(ip, BASE_PATH_V1, PATH_HEALTH);
    }

    /**
     * Lấy URL lấy giá trị nhiệt độ hiện tại của sensor theo naturalId.
     * * @param ip Địa chỉ IP của thiết bị/gateway
     * @param naturalId Định danh duy nhất của cảm biến
     * @return URL lấy giá trị nhiệt độ
     */
    public static String getTelemetryTempV1(String ip, String naturalId) {
        return build(ip, BASE_PATH_V1, String.format(PATH_TEMP, naturalId));
    }

    /**
     * Lấy URL lấy giá trị công suất điện hiện tại của sensor theo naturalId.
     * * @param ip Địa chỉ IP của thiết bị/gateway
     * @param naturalId Định danh duy nhất của cảm biến
     * @return URL lấy giá trị công suất
     */
    public static String getTelemetryPowerV1(String ip, String naturalId) {
        return build(ip, BASE_PATH_V1, String.format(PATH_POWER, naturalId));
    }

    /**
     * Lấy URL lấy tổng hợp các giá trị telemetry từ gateway.
     * * @param ip Địa chỉ IP của thiết bị/gateway
     * @return URL lấy dữ liệu telemetry tổng quát
     */
    public static String getTelemetryByGatewayV1(String ip) {
        return build(ip, BASE_PATH_V1, PATH_BATCH_TELEMETRY);
    }

    /**
     * Lấy URL điều khiển thiết bị theo naturalId.
     * * @param ip Địa chỉ IP của thiết bị/gateway
     * @param naturalId Định danh duy nhất của thiết bị điều khiển
     * @return URL lệnh điều khiển
     */
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