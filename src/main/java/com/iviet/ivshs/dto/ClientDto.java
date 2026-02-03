package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.enumeration.ClientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {

    private Long id;
    private String username;
    private ClientType clientType;
    private String ipAddress;
    private String macAddress;
    private String avatarUrl;
    private Date lastLoginAt;

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
        entity.setId(dto.getId());
        entity.setUsername(dto.getUsername());
        entity.setClientType(dto.getClientType());
        entity.setIpAddress(dto.getIpAddress());
        entity.setMacAddress(dto.getMacAddress());
        entity.setAvatarUrl(dto.getAvatarUrl());
        entity.setLastLoginAt(dto.getLastLoginAt());
        return entity;
    }
}
