package com.iviet.ivshs.integration.gateway;

import java.util.Optional;

public record GatewayFetchResult<T>(boolean success, T data, String message) {

    public static <T> GatewayFetchResult<T> ok(T data) {
        return new GatewayFetchResult<>(true, data, null);
    }

    public static <T> GatewayFetchResult<T> failure(String message) {
        return new GatewayFetchResult<>(false, null, message);
    }

    public static <T> GatewayFetchResult<T> notSupported(String reason) {
        return new GatewayFetchResult<>(false, null, "Not supported: " + reason);
    }

    public Optional<T> getData() {
        return Optional.ofNullable(data);
    }
}
