package com.iviet.ivshs.entities;

import org.quartz.Job;
import org.quartz.JobDataMap;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;


@MappedSuperclass
@Getter
@Setter
public abstract class BaseSchedulableEntity extends BaseAuditEntity {

  @Column(name = "is_interval", nullable = false)
  private Boolean isInterval;

  @Column(name = "cron_expression", nullable = true)
  private String cronExpression;

  @Column(name = "interval_seconds", nullable = true)
  private Integer intervalSeconds;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

  public abstract String getJobName();
  public abstract String getJobGroup();
  public abstract Class<? extends Job> getJobClass();
  public abstract JobDataMap getJobDataMap();
}
