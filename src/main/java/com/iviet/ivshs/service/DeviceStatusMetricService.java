package com.iviet.ivshs.service;

import com.iviet.ivshs.service.strategy.MetricServiceStrategy;

public interface DeviceStatusMetricService extends MetricServiceStrategy {
    void backupDeviceStatuses();
}
