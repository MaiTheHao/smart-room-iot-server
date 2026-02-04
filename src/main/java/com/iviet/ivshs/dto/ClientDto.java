package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.enumeration.ClientType;
import lombok.Builder;
import java.util.Date;

@Builder
public record ClientDto(
    Long id,
    String username,
    ClientType clientType,
    String ipAddress,
    String macAddress,
    String avatarUrl,
    Date lastLoginAt
) {
    public static ClientDto fromEntity(Client entity) {
        if (entity == null) return null;
        return ClientDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .clientType(entity.getClientType())
                .ipAddress(entity.getIpAddress())
                .macAddress(entity.getMacAddress())
                .avatarUrl(entity.getAvatarUrl())
                .lastLoginAt(entity.getLastLoginAt())
                .build();
    }

    public static Client toEntity(ClientDto dto) {
        if (dto == null) return null;
        Client entity = new Client();
        entity.setId(dto.id());
        entity.setUsername(dto.username()); 
        entity.setClientType(dto.clientType());
        entity.setIpAddress(dto.ipAddress());
        entity.setMacAddress(dto.macAddress());
        entity.setAvatarUrl(dto.avatarUrl());
        entity.setLastLoginAt(dto.lastLoginAt());
        return entity;
    }
}