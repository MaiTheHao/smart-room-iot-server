package com.iviet.ivshs.service;

import com.iviet.ivshs.enumeration.DeviceCategory;
public interface DeviceControlStrategy<T> {

  DeviceCategory getSupportedCategory();
  
  Class<T> getControlDtoClass();

  void control(String naturalId, T controlDto);
  void control(Long id, T controlDto);
}
