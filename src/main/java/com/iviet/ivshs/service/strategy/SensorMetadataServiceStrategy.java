package com.iviet.ivshs.service.strategy;

import java.util.List;

import com.iviet.ivshs.dto.SensorMetadataDto;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public interface SensorMetadataServiceStrategy {
    DeviceCategory getSupportedCategory();
    List<SensorMetadataDto> getSensorByRoomId(Long roomId);
    List<SensorMetadataDto> getAllSensor();
}
