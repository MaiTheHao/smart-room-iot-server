package com.iviet.ivshs.dto.sensor;

import com.iviet.ivshs.dto.powerconsumption.PowerConsumptionDto;
import com.iviet.ivshs.dto.temperature.TemperatureDto;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public record SensorMetadataDto(
    Long id,
    String naturalId,
    String name,
    String description,
    Boolean isActive,
    Long roomId,
    DeviceCategory category,
    Object sensor
) {
    public static SensorMetadataDto fromTemperature(TemperatureDto dto) {
        return new SensorMetadataDto(
            dto.id(), dto.naturalId(), dto.name(), dto.description(),
            dto.isActive(), dto.roomId(), DeviceCategory.TEMPERATURE, dto
        );
    }

    public static SensorMetadataDto fromPowerConsumption(PowerConsumptionDto dto) {
        return new SensorMetadataDto(
            dto.id(), dto.naturalId(), dto.name(), dto.description(),
            dto.isActive(), dto.roomId(), DeviceCategory.POWER_CONSUMPTION, dto
        );
    }
}
