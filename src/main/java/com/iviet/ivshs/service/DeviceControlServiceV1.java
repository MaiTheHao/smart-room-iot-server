package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.CreateDeviceControlDtoV1;
import com.iviet.ivshs.dto.DeviceControlDtoV1;
import com.iviet.ivshs.dto.PaginatedResponseV1;
import com.iviet.ivshs.dto.UpdateDeviceControlDtoV1;

public interface DeviceControlServiceV1 {
    DeviceControlDtoV1 getById(Long deviceControlId);
    DeviceControlDtoV1 create(CreateDeviceControlDtoV1 deviceControl);
    DeviceControlDtoV1 update(Long deviceControlId, UpdateDeviceControlDtoV1 deviceControl);
    void delete(Long deviceControlId);
    PaginatedResponseV1<DeviceControlDtoV1> getListByClientId(Long clientId, int page, int size);
    PaginatedResponseV1<DeviceControlDtoV1> getListByRoomId(Long roomId, int page, int size);
    Long countByRoomId(Long roomId);
}
