package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.DeviceMetadataDto;
import com.iviet.ivshs.enumeration.DeviceCategory;

public interface DeviceMetadataService {
  
  List<DeviceMetadataDto> getAll();

  List<DeviceMetadataDto> getAllByRoomId(Long roomId, DeviceCategory category);
  
  List<DeviceMetadataDto> getAllByClientId(Long clientId);

  Long getCountByRoomId(Long roomId);

  Long getCountByClientId(Long clientId);
}
