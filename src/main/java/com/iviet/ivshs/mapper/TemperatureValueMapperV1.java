package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.CreateTemperatureValueDtoV1;
import com.iviet.ivshs.dto.TemperatureValueDtoV1;
import com.iviet.ivshs.entities.TemperatureValueV1;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TemperatureValueMapperV1 {

    @Mapping(target = "sensorId", source = "entity.sensor.id")
    TemperatureValueDtoV1 toDto(TemperatureValueV1 entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sensor", ignore = true)
    @Mapping(target = "tempC", source = "dto.tempC")
    @Mapping(target = "timestamp", source = "dto.timestamp")
    TemperatureValueV1 fromCreateDto(CreateTemperatureValueDtoV1 dto);
}