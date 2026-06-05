package com.iviet.ivshs.dao.base;

import com.iviet.ivshs.entities.base.BaseIoTSensor;

public abstract class BaseIoTSensorDao<T extends BaseIoTSensor<?>> extends BaseIoTEntityDao<T> {

  protected BaseIoTSensorDao(Class<T> clazz) {
    super(clazz);
  }
}
