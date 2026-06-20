package com.iviet.ivshs.service.clientdevice;

import com.iviet.ivshs.dto.clientdevice.RegisterClientDeviceDto;

public interface ClientDeviceService {
    void registerDevice(RegisterClientDeviceDto request);
    void logoutDevice(Long clientId, String deviceIdentifier, com.iviet.ivshs.shared.enumeration.Platform platform);
}
