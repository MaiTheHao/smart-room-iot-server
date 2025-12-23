package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.iviet.ivshs.dto.ClientDtoV1;
import com.iviet.ivshs.dto.CreateClientDtoV1;
import com.iviet.ivshs.dto.UpdateClientDtoV1;
import com.iviet.ivshs.entities.ClientV1;
import com.iviet.ivshs.annotation.IgnoreAuditFields;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClientMapperV1 {

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "username", source = "entity.username")
    @Mapping(target = "clientType", source = "entity.clientType")
    @Mapping(target = "ipAddress", source = "entity.ipAddress")
    @Mapping(target = "macAddress", source = "entity.macAddress")
    @Mapping(target = "avatarUrl", source = "entity.avatarUrl")
    @Mapping(target = "lastLoginAt", source = "entity.lastLoginAt")
    ClientDtoV1 toDto(ClientV1 entity);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "deviceControls", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    ClientV1 toEntity(CreateClientDtoV1 dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "deviceControls", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    void updateEntityFromDto(UpdateClientDtoV1 dto, @MappingTarget ClientV1 entity);
}
