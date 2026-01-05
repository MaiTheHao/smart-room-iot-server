package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.iviet.ivshs.annotation.IgnoreAuditFields;
import com.iviet.ivshs.dto.CreateSysGroupDtoV1;
import com.iviet.ivshs.dto.SysGroupDtoV1;
import com.iviet.ivshs.dto.UpdateSysGroupDtoV1;
import com.iviet.ivshs.entities.SysGroupLanV1;
import com.iviet.ivshs.entities.SysGroupV1;

@Mapper(componentModel = "spring")
public interface SysGroupMapperV1 {

    /**
     * Convert Entity + Translation sang DTO
     */
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "groupCode", source = "entity.groupCode")
    @Mapping(target = "name", source = "groupLan.name")
    @Mapping(target = "description", source = "groupLan.description")
    SysGroupDtoV1 toDto(SysGroupV1 entity, SysGroupLanV1 groupLan);

    /**
     * Convert DTO sang Entity (dùng cho update)
     */
    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "clients", ignore = true)
    SysGroupV1 toEntity(SysGroupDtoV1 dto);

    /**
     * Convert CreateDTO sang Entity
     */
    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "clients", ignore = true)
    SysGroupV1 fromCreateDto(CreateSysGroupDtoV1 dto);

    /**
     * Convert UpdateDTO sang Entity
     */
    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "clients", ignore = true)
    SysGroupV1 fromUpdateDto(UpdateSysGroupDtoV1 dto);
}
