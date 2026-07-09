package com.iviet.ivshs.service.control;

import com.iviet.ivshs.dto.ControlDeviceResult;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public interface DeviceControlServiceStrategy<T> {

  DeviceCategory getSupportedCategory();

  Class<T> getControlDtoClass();

  ControlDeviceResult control(String naturalId, T controlDto);

  ControlDeviceResult control(Long id, T controlDto);
}
