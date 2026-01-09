package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.iviet.ivshs.annotation.IgnoreAuditFields;
import com.iviet.ivshs.dto.CreateRoomDto;
import com.iviet.ivshs.dto.RoomDto;
import com.iviet.ivshs.entities.RoomLan;
import com.iviet.ivshs.entities.Room;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "code", source = "entity.code")
    @Mapping(target = "name", source = "roomLan.name")
    @Mapping(target = "description", source = "roomLan.description")
    @Mapping(target = "floorId", source = "entity.floor.id")
    RoomDto toDto(Room entity, RoomLan roomLan);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "lights", ignore = true)
    @Mapping(target = "temperatures", ignore = true)
    @Mapping(target = "powerConsumptions", ignore = true)
    @Mapping(target = "deviceControls", ignore = true)
    @Mapping(target = "floor", ignore = true)
    Room toEntity(RoomDto dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "lights", ignore = true)
    @Mapping(target = "temperatures", ignore = true)
    @Mapping(target = "powerConsumptions", ignore = true)
    @Mapping(target = "deviceControls", ignore = true)
    @Mapping(target = "floor", ignore = true)
    Room fromCreateDto(CreateRoomDto dto);
}