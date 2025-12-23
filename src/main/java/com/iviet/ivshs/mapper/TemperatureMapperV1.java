package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.iviet.ivshs.dto.CreateTemperatureDtoV1;
import com.iviet.ivshs.dto.UpdateTemperatureDtoV1;
import com.iviet.ivshs.dto.TemperatureDtoV1;
import com.iviet.ivshs.entities.TemperatureV1;
import com.iviet.ivshs.entities.TemperatureLanV1;
import com.iviet.ivshs.annotation.IgnoreAuditFields;

@Mapper(componentModel = "spring")
public interface TemperatureMapperV1 {
    
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "name", source = "sensorLan.name")
    @Mapping(target = "description", source = "sensorLan.description")
    @Mapping(target = "isActive", source = "entity.isActive")
    @Mapping(target = "currentValue", source = "entity.currentValue")
    @Mapping(target = "naturalId", source = "entity.naturalId")
    @Mapping(target = "roomId", source = "entity.room.id")
    TemperatureDtoV1 toDto(TemperatureV1 entity, TemperatureLanV1 sensorLan);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sensorLans", ignore = true)
    @Mapping(target = "temperatureValues", ignore = true)
    @Mapping(target = "deviceControl", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "naturalId", source = "dto.naturalId")
    TemperatureV1 toEntity(TemperatureDtoV1 dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sensorLans", ignore = true)
    @Mapping(target = "currentValue", ignore = true)
    @Mapping(target = "temperatureValues", ignore = true)
    @Mapping(target = "deviceControl", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "naturalId", source = "dto.naturalId")
    TemperatureV1 fromCreateDto(CreateTemperatureDtoV1 dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sensorLans", ignore = true)
    @Mapping(target = "currentValue", ignore = true)
    @Mapping(target = "temperatureValues", ignore = true)
    @Mapping(target = "deviceControl", ignore = true)
    @Mapping(target = "room", ignore = true)
    TemperatureV1 fromUpdateDto(UpdateTemperatureDtoV1 dto);
}