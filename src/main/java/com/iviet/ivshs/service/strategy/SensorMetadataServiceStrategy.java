package com.iviet.ivshs.service.strategy;

import java.util.List;

import com.iviet.ivshs.dto.SensorMetadataDto;
import com.iviet.ivshs.entities.base.BaseIoTSensor;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public interface SensorMetadataServiceStrategy<T extends BaseIoTSensor<?>> {
    DeviceCategory getSupportedCategory();
    List<SensorMetadataDto> getSensorMetadataByRoomId(Long roomId);
    List<SensorMetadataDto> getAllSensorMetadata();
    SensorMetadataDto getSensorMetadataById(Long id);
    SensorMetadataDto getSensorMetadataByNaturalId(String naturalId);
    T getSensorById(Long id);
    T getSensorByNaturalId(String naturalId);
}
