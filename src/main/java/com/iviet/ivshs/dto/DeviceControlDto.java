package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.enumeration.DeviceControlType;
import lombok.Builder;

@Builder
public record DeviceControlDto(
    Long id,
    DeviceControlType controlType,
    Integer gpioPin,
    String bleMacAddress,
    String apiEndpoint,
    Long clientId,
    Long roomId
) {
    public static DeviceControlDto from(HardwareConfig entity) {
        return DeviceControlDto.builder()
            .id(entity.getId())
            .controlType(entity.getControlType())
            .gpioPin(entity.getGpioPin())
            .bleMacAddress(entity.getBleMacAddress())
            .apiEndpoint(entity.getApiEndpoint())
            .clientId(entity.getClient() != null ? entity.getClient().getId() : null)
            .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
            .build();
    }
}
