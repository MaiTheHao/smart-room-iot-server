package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.enumeration.DeviceControlType;
import lombok.Builder;

@Builder
public record DeviceControlDto(
    Long id,
    DeviceControlType deviceControlType,
    Integer gpioPin,
    String bleMacAddress,
    String apiEndpoint,
    Long clientId,
    Long roomId
) {
    public static DeviceControlDto from(DeviceControl entity) {
        return DeviceControlDto.builder()
            .id(entity.getId())
            .deviceControlType(entity.getDeviceControlType())
            .gpioPin(entity.getGpioPin())
            .bleMacAddress(entity.getBleMacAddress())
            .apiEndpoint(entity.getApiEndpoint())
            .clientId(entity.getClient() != null ? entity.getClient().getId() : null)
            .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
            .build();
    }
}
