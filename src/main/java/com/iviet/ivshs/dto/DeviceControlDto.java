package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.DeviceControlTypeV1;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceControlDto {
    
    private Long id;
    private DeviceControlTypeV1 deviceControlType;
    private Integer gpioPin;
    private String bleMacAddress;
    private String apiEndpoint;
    private Long clientId;
    private Long roomId;
}
