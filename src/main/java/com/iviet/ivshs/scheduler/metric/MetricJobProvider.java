package com.iviet.ivshs.scheduler.metric;

import java.util.List;

public interface MetricJobProvider {

    List<MetricJobRegistration> getMetricJobs();
}
