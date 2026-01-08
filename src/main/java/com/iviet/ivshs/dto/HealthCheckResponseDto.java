package com.iviet.ivshs.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@lombok.Data
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthCheckResponseDto {
    private int status;
    private String message;
    private Data data;
    private String timestamp;

    @Builder
    @lombok.Data
    @Jacksonized
    public static class Data {
        private String roomCode;
        // private String ipAddress;
        private List<DeviceDto> devices;
    }

    @Builder
    @lombok.Data
    @Jacksonized
    public static class DeviceDto {
        private String naturalId;
        private String category;
        // private String controlType;
        // private String bleMac;
        // private int gpioPin;
        private boolean isActive;
    }
}