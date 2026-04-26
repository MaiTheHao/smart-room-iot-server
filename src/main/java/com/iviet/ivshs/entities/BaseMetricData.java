package com.iviet.ivshs.entities;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseMetricData extends BaseEntity {

  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(name = "target_category", nullable = false, length = 50)
  protected String targetCategory;

  @Column(name = "target_id", nullable = false)
  protected Long targetId;

  @Column(name = "timestamp", nullable = false, updatable = false)
  protected Instant timestamp;

  @Column(name = "unix_minute", insertable = false, updatable = false)  
  protected Integer unixMinute;

  public Instant setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
    this.unixMinute = (int) (timestamp.getEpochSecond() / 60);
    return this.timestamp;
  }

  abstract public void setTargetCategory(String targetCategory);
}
