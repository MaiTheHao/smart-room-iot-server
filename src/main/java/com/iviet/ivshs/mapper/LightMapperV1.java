package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.iviet.ivshs.dto.CreateLightDtoV1;
import com.iviet.ivshs.dto.LightDtoV1;
import com.iviet.ivshs.entities.LightLanV1;
import com.iviet.ivshs.entities.LightV1;
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
    LightDtoV1 toDto(LightV1 entity, LightLanV1 lightLan);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "deviceControl", ignore = true)
    @Mapping(target = "naturalId", source = "dto.naturalId")
    LightV1 toEntity(LightDtoV1 dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "deviceControl", ignore = true)
    @Mapping(target = "naturalId", source = "dto.naturalId")
    LightV1 fromCreateDto(CreateLightDtoV1 dto);
}