package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.iviet.ivshs.dto.CreateLightDto;
import com.iviet.ivshs.dto.LightDto;
import com.iviet.ivshs.entities.LightLan;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.annotation.IgnoreAuditFields;

@Mapper(componentModel = "spring")
public interface LightMapperV1 {
    
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "naturalId", source = "entity.naturalId")
    @Mapping(target = "name", source = "lightLan.name")
    @Mapping(target = "description", source = "lightLan.description")
    @Mapping(target = "isActive", source = "entity.isActive")
    @Mapping(target = "level", source = "entity.level")
    @Mapping(target = "roomId", source = "entity.room.id")
    LightDto toDto(Light entity, LightLan lightLan);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "deviceControl", ignore = true)
    @Mapping(target = "naturalId", source = "dto.naturalId")
    Light toEntity(LightDto dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "deviceControl", ignore = true)
    @Mapping(target = "naturalId", source = "dto.naturalId")
    Light fromCreateDto(CreateLightDto dto);
}