package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.CreateDeviceControlDto;
import com.iviet.ivshs.dto.DeviceControlDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateDeviceControlDto;

public interface DeviceControlServiceV1 {
    DeviceControlDto getById(Long deviceControlId);
    DeviceControlDto create(CreateDeviceControlDto deviceControl);
    DeviceControlDto update(Long deviceControlId, UpdateDeviceControlDto deviceControl);
    void delete(Long deviceControlId);
    PaginatedResponse<DeviceControlDto> getListByClientId(Long clientId, int page, int size);
    PaginatedResponse<DeviceControlDto> getListByRoomId(Long roomId, int page, int size);
    Long countByRoomId(Long roomId);
}
