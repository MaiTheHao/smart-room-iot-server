package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.iviet.ivshs.dto.CreateFloorDtoV1;
import com.iviet.ivshs.dto.FloorDtoV1;
import com.iviet.ivshs.entities.FloorLanV1;
import com.iviet.ivshs.entities.FloorV1;
import com.iviet.ivshs.annotation.IgnoreAuditFields;

@Mapper(componentModel = "spring")
public interface FloorMapperV1 {

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "code", source = "entity.code")
    @Mapping(target = "name", source = "floorLan.name")
    @Mapping(target = "description", source = "floorLan.description")
    @Mapping(target = "level", source = "entity.level")
    FloorDtoV1 toDto(FloorV1 entity, FloorLanV1 floorLan);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    FloorV1 toEntity(FloorDtoV1 dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    FloorV1 fromCreateDto(CreateFloorDtoV1 dto);
}
