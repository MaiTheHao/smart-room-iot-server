package com.iviet.ivshs.service;

import com.iviet.ivshs.entities.base.BaseSchedulableEntity;

public interface SchedulableJobService<T extends BaseSchedulableEntity> {
    void scheduleJob(T entity);

    void unscheduleJob(Long id);

    void triggerNow(Long id);

    void reloadAll();
}
