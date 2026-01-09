package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.iviet.ivshs.annotation.IgnoreAuditFields;
import com.iviet.ivshs.dto.CreateSysGroupDto;
import com.iviet.ivshs.dto.SysGroupDto;
import com.iviet.ivshs.dto.UpdateSysGroupDto;
import com.iviet.ivshs.entities.SysGroupLan;
import com.iviet.ivshs.entities.SysGroup;

@Mapper(componentModel = "spring")
public interface SysGroupMapper {

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "groupCode", source = "entity.groupCode")
    @Mapping(target = "name", source = "groupLan.name")
    @Mapping(target = "description", source = "groupLan.description")
    SysGroupDto toDto(SysGroup entity, SysGroupLan groupLan);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "clients", ignore = true)
    SysGroup toEntity(SysGroupDto dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "clients", ignore = true)
    SysGroup fromCreateDto(CreateSysGroupDto dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "clients", ignore = true)
    @Mapping(target = "groupCode", ignore = true)
    SysGroup fromUpdateDto(UpdateSysGroupDto dto);
}
