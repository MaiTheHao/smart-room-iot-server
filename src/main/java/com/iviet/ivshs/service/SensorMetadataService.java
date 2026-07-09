package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.SensorMetadataDto;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public interface SensorMetadataService {
    List<SensorMetadataDto> getAllByRoomId(Long roomId, DeviceCategory category);
    List<SensorMetadataDto> getAll(DeviceCategory category);
    Long getCountByRoomId(Long roomId);
}
