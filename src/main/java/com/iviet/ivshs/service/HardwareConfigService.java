package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.CreateDeviceControlDto;
import com.iviet.ivshs.dto.DeviceControlDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateDeviceControlDto;

public interface HardwareConfigService {
    DeviceControlDto getById(Long deviceControlId);
    DeviceControlDto create(CreateDeviceControlDto hardwareConfig);
    DeviceControlDto update(Long deviceControlId, UpdateDeviceControlDto hardwareConfig);
    void delete(Long deviceControlId);
    PaginatedResponse<DeviceControlDto> getListByClientId(Long clientId, int page, int size);
    PaginatedResponse<DeviceControlDto> getListByRoomId(Long roomId, int page, int size);
    Long countByRoomId(Long roomId);
}
