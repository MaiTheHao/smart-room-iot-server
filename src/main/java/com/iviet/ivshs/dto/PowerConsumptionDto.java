package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.PowerConsumption;
import com.iviet.ivshs.entities.PowerConsumptionLan;
import lombok.Builder;

@Builder
public record PowerConsumptionDto(
    Long id,
    String name,
    String description,
    Boolean isActive,
    Double currentWatt,
    String naturalId,
    Long roomId
) {
    public static PowerConsumptionDto from(PowerConsumption entity, PowerConsumptionLan powerLan) {
        return PowerConsumptionDto.builder()
            .id(entity.getId())
            .name(powerLan.getName())
            .description(powerLan.getDescription())
            .isActive(entity.getIsActive())
            .currentWatt(entity.getCurrentWatt())
            .naturalId(entity.getNaturalId())
            .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
            .build();
    }
}
