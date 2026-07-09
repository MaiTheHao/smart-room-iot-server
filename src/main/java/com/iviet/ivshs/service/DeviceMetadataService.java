package com.iviet.ivshs.service.control;

import java.util.List;

import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public interface DeviceMetadataService {

  List<Object> getAllByRoomId(Long roomId, DeviceCategory category);

  List<Object> getAll(DeviceCategory category);

  Long getCountByRoomId(Long roomId);

}
