package com.iviet.ivshs.dto;

import org.hibernate.validator.constraints.URL;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.enumeration.ClientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClientDto {

    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    private ClientType clientType;

    @Size(max = 255, message = "IP address must not exceed 255 characters")
    @Pattern(
        regexp = "^(([0-9]{1,3}\\.){3}[0-9]{1,3}(:[0-9]{1,5})?|([0-9a-fA-F:]+)|(\\[[0-9a-fA-F:]+\\]:[0-9]{1,5}))$",
        message = "Invalid IP address format"
    )
    private String ipAddress;

    @Size(max = 100, message = "MAC address must not exceed 100 characters")
    @Pattern(
        regexp = "^([0-9A-Fa-f]{2}[:]){5}[0-9A-Fa-f]{2}$|^[0-9A-Fa-f]{12}$",
        message = "Invalid MAC address format"
    )
    private String macAddress;

    @URL(message = "Invalid avatar URL")
    @Size(max = 255, message = "Avatar URL must not exceed 255 characters")
    private String avatarUrl;

    public static void applyToEntity(UpdateClientDto dto, Client entity) {
        if (dto == null || entity == null) return;

        entity.setClientType(dto.getClientType());
        entity.setIpAddress(dto.getIpAddress());
        entity.setMacAddress(dto.getMacAddress());
        entity.setAvatarUrl(dto.getAvatarUrl());
    }
}
