package com.iviet.ivshs.dto;

import org.hibernate.validator.constraints.URL;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.enumeration.ClientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateClientDto(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    String username,

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    String password,

    @NotNull(message = "Client type is required")
    ClientType clientType,

    @Size(max = 255, message = "IPv4 address must not exceed 255 characters")
    @Pattern(
        regexp = "^(([0-9]{1,3}\\.){3}[0-9]{1,3}(:[0-9]{1,5})?|([0-9a-fA-F:]+)|(\\[[0-9a-fA-F:]+\\]:[0-9]{1,5}))$",
        message = "Invalid IPv4 address format"
    )
    String ipAddress,

    @Size(max = 100, message = "MAC address must not exceed 100 characters")
    @Pattern(
        regexp = "^([0-9A-Fa-f]{2}[:]){5}[0-9A-Fa-f]{2}$|^[0-9A-Fa-f]{12}$",
        message = "Invalid MAC address format"
    )
    String macAddress,

    @URL(message = "Invalid avatar URL")
    @Size(max = 255, message = "Avatar URL must not exceed 255 characters")
    String avatarUrl
) {
    public static Client toEntity(CreateClientDto dto) {
        if (dto == null) return null;
        Client client = new Client();
        client.setUsername(dto.username());
        client.setPasswordHash(dto.password());
        client.setClientType(dto.clientType());
        client.setIpAddress(dto.ipAddress());
        client.setMacAddress(dto.macAddress());
        client.setAvatarUrl(dto.avatarUrl());
        return client;
    }
}
