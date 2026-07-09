package com.iviet.ivshs.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.iviet.ivshs.entities.base.BaseSchedulableEntity;
import com.iviet.ivshs.service.JobScheduleService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSchedulableJobService<T extends BaseSchedulableEntity> implements SchedulableJobService<T> {

    @Autowired
    protected JobScheduleService jobScheduleService;

    protected abstract T getEntityById(Long id);

    protected abstract List<T> getAllActiveEntities();

    protected abstract String getJobGroup();

    @Override
    public void scheduleJob(T entity) {
        jobScheduleService.sync(entity);
    }

    @Override
    public void unscheduleJob(Long id) {
        jobScheduleService.delete(getEntityById(id));
    }

    @Override
    @Transactional
    public void triggerNow(Long id) {
        jobScheduleService.triggerNow(getEntityById(id));
    }

    @Override
    @Transactional
    public void reloadAll() {
        jobScheduleService.deleteJobGroup(getJobGroup());
        List<T> actives = getAllActiveEntities();
        for (T entity : actives) {
            try {
                jobScheduleService.sync(entity);
            } catch (Exception e) {
                log.error("Failed to reload job: id={}", entity.getId(), e);
            }
        }
    }
}
