package com.iviet.ivshs.schedule.metric;

import java.util.List;

public interface MetricJobProvider {

    List<MetricJobRegistration> getMetricJobs();
}
