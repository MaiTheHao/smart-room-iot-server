package com.iviet.ivshs.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthCheckResponseDto {
    private int status;
    private String message;
    private Data data;
    private String timestamp;

    @Builder
    @lombok.Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {
        private String roomCode;
        // private String ipAddress;
        private List<DeviceDto> devices;
    }

    @Builder
    @lombok.Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeviceDto {
        private String naturalId;
        private String category;
        // private String controlType;
        // private String bleMac;
        // private int gpioPin;
        private boolean isActive;
    }
}