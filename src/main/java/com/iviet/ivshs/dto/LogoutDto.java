package com.iviet.ivshs.dto;

import com.iviet.ivshs.shared.enumeration.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutDto {

    @NotBlank(message = "Device identifier cannot be blank")
    private String deviceIdentifier;

    @NotNull(message = "Platform cannot be null")
    private Platform platform;
}
