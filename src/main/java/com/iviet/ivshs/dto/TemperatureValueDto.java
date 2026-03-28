package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.TemperatureValue;
import lombok.Builder;
import java.time.Instant;

@Builder
public record TemperatureValueDto(
    Long id,
    Long sensorId,
    Double tempC,
    Instant timestamp
) {
    public static TemperatureValueDto from(TemperatureValue entity) {
        return TemperatureValueDto.builder()
            .id(entity.getId())
            .sensorId(entity.getSensor() != null ? entity.getSensor().getId() : null)
            .tempC(entity.getTempC())
            .timestamp(entity.getTimestamp())
            .build();
    }
}