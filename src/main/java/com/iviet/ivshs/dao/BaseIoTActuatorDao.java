package com.iviet.ivshs.dao;

import com.iviet.ivshs.entities.BaseIoTActuator;

public abstract class BaseIoTActuatorDao<T extends BaseIoTActuator<?>> extends BaseIoTEntityDao<T> {

  protected BaseIoTActuatorDao(Class<T> clazz) {
    super(clazz);
  }
}
