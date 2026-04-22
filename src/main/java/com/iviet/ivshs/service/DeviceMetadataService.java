package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.enumeration.DeviceCategory;

public interface DeviceMetadataService {
  

  List<Object> getAllByRoomId(Long roomId, DeviceCategory category);
  


  Long getCountByRoomId(Long roomId);

}
