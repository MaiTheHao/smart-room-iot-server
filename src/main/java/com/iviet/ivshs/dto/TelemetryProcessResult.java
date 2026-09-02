package com.iviet.ivshs.dto;

public record TelemetryProcessResult(
    int totalDevices,
    int successCount,
    int skippedCount,
    int failedCount
) {
    public static TelemetryProcessResult empty() {
        return new TelemetryProcessResult(0, 0, 0, 0);
    }
}
