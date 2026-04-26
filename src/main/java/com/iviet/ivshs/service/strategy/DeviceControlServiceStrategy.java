package com.iviet.ivshs.service.strategy;

import com.iviet.ivshs.enumeration.DeviceCategory;
public interface DeviceControlServiceStrategy<T> {

  DeviceCategory getSupportedCategory();
  
  Class<T> getControlDtoClass();

  void control(String naturalId, T controlDto);
  
  void control(Long id, T controlDto);
}
