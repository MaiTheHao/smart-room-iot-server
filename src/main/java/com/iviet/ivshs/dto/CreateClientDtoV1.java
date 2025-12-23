package com.iviet.ivshs.dto;

import org.hibernate.validator.constraints.URL;

import com.iviet.ivshs.enumeration.ClientTypeV1;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClientDtoV1 {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotNull(message = "Client type is required")
    private ClientTypeV1 clientType;

    @Size(max = 45, message = "IP address must not exceed 45 characters")
    @Pattern(
        regexp = "^((25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(?!$)|$)){4}$"
                + "|"
                + "^[0-9A-Fa-f:]{2,45}$",
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
}
