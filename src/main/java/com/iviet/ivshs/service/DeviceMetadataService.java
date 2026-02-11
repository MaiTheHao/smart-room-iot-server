package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.DeviceMetadataDto;

public interface DeviceMetadataService {
  
  List<DeviceMetadataDto> getAll();

  List<DeviceMetadataDto> getAllByRoomId(Long roomId);
  
  List<DeviceMetadataDto> getAllByClientId(Long clientId);
}
