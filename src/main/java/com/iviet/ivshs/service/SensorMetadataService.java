package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.SensorMetadataDto;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public interface SensorMetadataService {
    List<SensorMetadataDto> getAllByRoomId(Long roomId, DeviceCategory category);
    List<SensorMetadataDto> getAll(DeviceCategory category);
    PaginatedResponse<SensorMetadataDto> getAllByRoomId(Long roomId, DeviceCategory category, int page, int size);
    PaginatedResponse<SensorMetadataDto> getAll(DeviceCategory category, int page, int size);
    Long getCountByRoomId(Long roomId);
    SensorMetadataDto getSensorById(Long id, DeviceCategory category);
    SensorMetadataDto getSensorByNaturalId(String naturalId, DeviceCategory category);
}
