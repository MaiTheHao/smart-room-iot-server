package com.iviet.ivshs.dao.base;

import com.iviet.ivshs.entities.base.BaseIoTDevice;

public abstract class BaseIoTActuatorDao<T extends BaseIoTDevice<?>> extends BaseIoTEntityDao<T> {

  protected BaseIoTActuatorDao(Class<T> clazz) {
    super(clazz);
  }
}
