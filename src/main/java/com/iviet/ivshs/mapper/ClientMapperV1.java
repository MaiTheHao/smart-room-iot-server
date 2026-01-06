package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.iviet.ivshs.dto.ClientDto;
import com.iviet.ivshs.dto.CreateClientDto;
import com.iviet.ivshs.dto.UpdateClientDto;
import com.iviet.ivshs.entities.Client;
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
    ClientDto toDto(Client entity);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "deviceControls", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    Client toEntity(CreateClientDto dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "deviceControls", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    void updateEntityFromDto(UpdateClientDto dto, @MappingTarget Client entity);
}
