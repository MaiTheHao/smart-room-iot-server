package com.iviet.ivshs.schedule.metric;

import lombok.Builder;
import lombok.Getter;
import org.quartz.Job;

@Builder
@Getter
public class MetricJobRegistration {
    private String name;
    private String group;
    private Class<? extends Job> jobClass;
    
    private Integer intervalSeconds;
    private String cronExpression;
}
