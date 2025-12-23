package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.DeviceControlTypeV1;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeviceControlDtoV1 {
    
    private DeviceControlTypeV1 deviceControlType;

    @Min(value = 0, message = "GPIO pin must be at least 0")
    @Max(value = 40, message = "GPIO pin must not exceed 40")
    private Integer gpioPin;

    @Size(max = 100, message = "BLUETOOTH MAC address must not exceed 100 characters")
    @Pattern(
        regexp = "^([0-9A-Fa-f]{2}[:]){5}[0-9A-Fa-f]{2}$|^[0-9A-Fa-f]{12}$",
        message = "Invalid MAC address format"
    )
    private String bleMacAddress;

    @Size(max = 255, message = "API endpoint must not exceed 255 characters")
    @Pattern(
        regexp = "^(https?://).+$",
        message = "API endpoint must start with http:// or https://"
    )
    private String apiEndpoint;

    private Long clientId;
    
    private Long roomId;
}
