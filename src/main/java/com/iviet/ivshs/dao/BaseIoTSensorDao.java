package com.iviet.ivshs.dao;

import com.iviet.ivshs.entities.BaseIoTSensor;

public abstract class BaseIoTSensorDao<T extends BaseIoTSensor<?>> extends BaseIoTEntityDao<T> {

  protected BaseIoTSensorDao(Class<T> clazz) {
    super(clazz);
  }
}
