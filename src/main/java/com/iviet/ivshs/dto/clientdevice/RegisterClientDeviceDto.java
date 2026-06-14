package com.iviet.ivshs.dto.clientdevice;

import com.iviet.ivshs.shared.enumeration.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterClientDeviceDto {

    @NotBlank(message = "FCM token cannot be blank")
    @Size(max = 512, message = "FCM token must be at most 512 characters")
    private String fcmToken;

    @NotBlank(message = "Device identifier cannot be blank")
    @Size(max = 255, message = "Device identifier must be at most 255 characters")
    private String deviceIdentifier;

    private Platform platform;
}
