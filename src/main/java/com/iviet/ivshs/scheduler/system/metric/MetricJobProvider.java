package com.iviet.ivshs.scheduler.system.metric;

import java.util.List;

public interface MetricJobProvider {

    List<MetricJobRegistration> getMetricJobs();
}
