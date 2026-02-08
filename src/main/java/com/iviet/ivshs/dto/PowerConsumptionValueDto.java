package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.PowerConsumptionValue;
import lombok.Builder;
import java.time.Instant;

@Builder
public record PowerConsumptionValueDto(
    Long id,
    Long sensorId,
    Double watt,
    Instant timestamp
) {
    public static PowerConsumptionValueDto from(PowerConsumptionValue entity) {
        return PowerConsumptionValueDto.builder()
            .id(entity.getId())
            .sensorId(entity.getSensor() != null ? entity.getSensor().getId() : null)
            .watt(entity.getWatt())
            .timestamp(entity.getTimestamp())
            .build();
    }
}
