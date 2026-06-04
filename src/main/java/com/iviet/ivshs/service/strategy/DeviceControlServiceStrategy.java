package com.iviet.ivshs.service.strategy;

import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.dto.ControlDeviceResult;

public interface DeviceControlServiceStrategy<T> {

  DeviceCategory getSupportedCategory();

  Class<T> getControlDtoClass();

  ControlDeviceResult control(String naturalId, T controlDto);

  ControlDeviceResult control(Long id, T controlDto);
}
