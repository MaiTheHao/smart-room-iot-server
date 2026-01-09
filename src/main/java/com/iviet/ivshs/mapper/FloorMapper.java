package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.iviet.ivshs.dto.CreateFloorDto;
import com.iviet.ivshs.dto.FloorDto;
import com.iviet.ivshs.entities.FloorLan;
import com.iviet.ivshs.entities.Floor;
import com.iviet.ivshs.annotation.IgnoreAuditFields;

@Mapper(componentModel = "spring")
public interface FloorMapper {

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "code", source = "entity.code")
    @Mapping(target = "name", source = "floorLan.name")
    @Mapping(target = "description", source = "floorLan.description")
    @Mapping(target = "level", source = "entity.level")
    FloorDto toDto(Floor entity, FloorLan floorLan);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    Floor toEntity(FloorDto dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    Floor fromCreateDto(CreateFloorDto dto);
}
