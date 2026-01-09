package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.CreateTemperatureValueDto;
import com.iviet.ivshs.dto.TemperatureValueDto;
import com.iviet.ivshs.entities.TemperatureValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TemperatureValueMapper {

    @Mapping(target = "sensorId", source = "entity.sensor.id")
    TemperatureValueDto toDto(TemperatureValue entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sensor", ignore = true)
    TemperatureValue fromCreateDto(CreateTemperatureValueDto dto);
}