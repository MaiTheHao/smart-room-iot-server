package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.iviet.ivshs.dto.CreateTemperatureDto;
import com.iviet.ivshs.dto.UpdateTemperatureDto;
import com.iviet.ivshs.dto.TemperatureDto;
import com.iviet.ivshs.entities.Temperature;
import com.iviet.ivshs.entities.TemperatureLan;
import com.iviet.ivshs.annotation.IgnoreAuditFields;

@Mapper(componentModel = "spring")
public interface TemperatureMapper {
    
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "name", source = "sensorLan.name")
    @Mapping(target = "description", source = "sensorLan.description")
    @Mapping(target = "isActive", source = "entity.isActive")
    @Mapping(target = "currentValue", source = "entity.currentValue")
    @Mapping(target = "naturalId", source = "entity.naturalId")
    @Mapping(target = "roomId", source = "entity.room.id")
    TemperatureDto toDto(Temperature entity, TemperatureLan sensorLan);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "temperatureValues", ignore = true)
    @Mapping(target = "deviceControl", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "naturalId", source = "dto.naturalId")
    Temperature toEntity(TemperatureDto dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "currentValue", ignore = true)
    @Mapping(target = "temperatureValues", ignore = true)
    @Mapping(target = "deviceControl", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "naturalId", source = "dto.naturalId")
    Temperature fromCreateDto(CreateTemperatureDto dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "naturalId", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "currentValue", ignore = true)
    @Mapping(target = "temperatureValues", ignore = true)
    @Mapping(target = "deviceControl", ignore = true)
    @Mapping(target = "room", ignore = true)
    Temperature fromUpdateDto(UpdateTemperatureDto dto);
}