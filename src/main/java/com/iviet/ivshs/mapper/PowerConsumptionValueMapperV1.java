package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.iviet.ivshs.dto.PowerConsumptionValueDtoV1;
import com.iviet.ivshs.dto.CreatePowerConsumptionValueDtoV1;
import com.iviet.ivshs.entities.PowerConsumptionValueV1;

@Mapper(componentModel = "spring")
public interface PowerConsumptionValueMapperV1 {

    @Mapping(target = "sensorId", source = "sensor.id")
    PowerConsumptionValueDtoV1 toDto(PowerConsumptionValueV1 entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sensor", ignore = true)
    PowerConsumptionValueV1 fromCreateDto(CreatePowerConsumptionValueDtoV1 dto);
}
