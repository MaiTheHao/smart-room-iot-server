package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.enumeration.DeviceControlType;
import lombok.Builder;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Builder
public record CreateDeviceControlDto(
    @NotNull(message = "Device control type is required")
    DeviceControlType deviceControlType,

    @Min(value = 0, message = "GPIO pin must be at least 0")
    @Max(value = 40, message = "GPIO pin must not exceed 40")
    Integer gpioPin,
    
    @Size(max = 100, message = "BLUETOOTH MAC address must not exceed 100 characters")
    @Pattern(
        regexp = "^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$",
        message = "Invalid BLUETOOTH MAC address format"
    )
    String bleMacAddress,

    @Size(max = 255, message = "API endpoint must not exceed 255 characters")
    @Pattern(
        regexp = "^(https?://).+$",
        message = "API endpoint must start with http:// or https://"
    )
    String apiEndpoint,

    @NotNull(message = "Client ID is required")
    Long clientId,

    @NotNull(message = "Room ID is required")
    Long roomId
) {
    public DeviceControl toEntity() {
        var deviceControl = new DeviceControl();
        deviceControl.setDeviceControlType(deviceControlType);
        deviceControl.setGpioPin(gpioPin);
        deviceControl.setBleMacAddress(bleMacAddress);
        deviceControl.setApiEndpoint(apiEndpoint);
        return deviceControl;
    }
}
