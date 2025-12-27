package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.iviet.ivshs.annotation.IgnoreAuditFields;
import com.iviet.ivshs.dto.CreateRoomDtoV1;
import com.iviet.ivshs.dto.RoomDtoV1;
import com.iviet.ivshs.entities.RoomLanV1;
import com.iviet.ivshs.entities.RoomV1;

@Mapper(componentModel = "spring")
public interface RoomMapperV1 {

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "code", source = "entity.code")
    @Mapping(target = "name", source = "roomLan.name")
    @Mapping(target = "description", source = "roomLan.description")
    @Mapping(target = "floorId", source = "entity.floor.id")
    RoomDtoV1 toDto(RoomV1 entity, RoomLanV1 roomLan);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "lights", ignore = true)
    @Mapping(target = "temperatures", ignore = true)
    @Mapping(target = "powerConsumptions", ignore = true)
    @Mapping(target = "deviceControls", ignore = true)
    @Mapping(target = "floor", ignore = true)
    RoomV1 toEntity(RoomDtoV1 dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "lights", ignore = true)
    @Mapping(target = "temperatures", ignore = true)
    @Mapping(target = "powerConsumptions", ignore = true)
    @Mapping(target = "deviceControls", ignore = true)
    @Mapping(target = "floor", ignore = true)
    RoomV1 fromCreateDto(CreateRoomDtoV1 dto);
}