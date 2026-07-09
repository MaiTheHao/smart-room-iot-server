package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.RegisterClientDeviceDto;

public interface ClientDeviceService {
    void registerDevice(RegisterClientDeviceDto request);
    void logoutDevice(Long clientId, String deviceIdentifier, com.iviet.ivshs.shared.enumeration.Platform platform);
}
