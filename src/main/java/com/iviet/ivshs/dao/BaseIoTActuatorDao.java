package com.iviet.ivshs.dao;

import com.iviet.ivshs.entities.BaseIoTDevice;

public abstract class BaseIoTActuatorDao<T extends BaseIoTDevice<?>> extends BaseIoTEntityDao<T> {

  protected BaseIoTActuatorDao(Class<T> clazz) {
    super(clazz);
  }
}
