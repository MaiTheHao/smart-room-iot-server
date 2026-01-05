package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.iviet.ivshs.annotation.IgnoreAuditFields;
import com.iviet.ivshs.dto.CreateSysFunctionDtoV1;
import com.iviet.ivshs.dto.SysFunctionDtoV1;
import com.iviet.ivshs.dto.UpdateSysFunctionDtoV1;
import com.iviet.ivshs.entities.SysFunctionLanV1;
import com.iviet.ivshs.entities.SysFunctionV1;

@Mapper(componentModel = "spring")
public interface SysFunctionMapperV1 {

    /**
     * Convert Entity + Translation sang DTO
     */
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "functionCode", source = "entity.functionCode")
    @Mapping(target = "name", source = "functionLan.name")
    @Mapping(target = "description", source = "functionLan.description")
    SysFunctionDtoV1 toDto(SysFunctionV1 entity, SysFunctionLanV1 functionLan);

    /**
     * Convert DTO sang Entity (dùng cho update)
     */
    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "roles", ignore = true)
    SysFunctionV1 toEntity(SysFunctionDtoV1 dto);

    /**
     * Convert CreateDTO sang Entity
     */
    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "roles", ignore = true)
    SysFunctionV1 fromCreateDto(CreateSysFunctionDtoV1 dto);

    /**
     * Convert UpdateDTO sang Entity
     */
    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "roles", ignore = true)
    SysFunctionV1 fromUpdateDto(UpdateSysFunctionDtoV1 dto);
}
