package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.iviet.ivshs.dto.PowerConsumptionValueDto;
import com.iviet.ivshs.dto.CreatePowerConsumptionValueDto;
import com.iviet.ivshs.entities.PowerConsumptionValue;

@Mapper(componentModel = "spring")
public interface PowerConsumptionValueMapperV1 {

    @Mapping(target = "sensorId", source = "sensor.id")
    PowerConsumptionValueDto toDto(PowerConsumptionValue entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sensor", ignore = true)
    PowerConsumptionValue fromCreateDto(CreatePowerConsumptionValueDto dto);
}
