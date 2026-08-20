package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.base.BaseMetricData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(
    name = "motion_metrics",
    indexes = {
      @Index(
          name = "idx_motion_metrics_target",
          columnList = "target_category, target_id, timestamp"),
      @Index(name = "idx_motion_metrics_timestamp", columnList = "timestamp"),
      @Index(name = "idx_motion_metrics_unix_minute", columnList = "unix_minute")
    })
@Immutable
@Getter
@Setter
@NoArgsConstructor
public class MotionMetric extends BaseMetricData {

  @Column(name = "motion_detected", nullable = false)
  private Boolean motionDetected;

  @Override
  public void setTargetCategory(String targetCategory) {
    if (targetCategory == null || targetCategory.isBlank()) {
      throw new IllegalArgumentException("Target category cannot be null or blank");
    }
    if (!"MOTION_DETECTOR".equals(targetCategory)) {
      throw new IllegalArgumentException("Invalid target category: " + targetCategory);
    }
    this.targetCategory = targetCategory;
  }
}
