package com.iviet.ivshs.integration.gateway;

public record GatewayOperationResult(boolean success, String message) {

    public static GatewayOperationResult ok() {
        return new GatewayOperationResult(true, "Success");
    }

    public static GatewayOperationResult failure(String message) {
        return new GatewayOperationResult(false, message);
    }

    public static GatewayOperationResult notSupported(String reason) {
        return new GatewayOperationResult(false, "Not supported: " + reason);
    }
}
