package com.iviet.ivshs.scheduler.dynamic.base;

public interface SchedulableJobProcessor {
    JobProcessorType getProcessorType();
    void processJob(Long id);
}
