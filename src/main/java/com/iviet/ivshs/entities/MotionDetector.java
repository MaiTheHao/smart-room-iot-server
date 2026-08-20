package com.iviet.ivshs.entities;

import com.iviet.ivshs.dto.MotionDetectorData;
import com.iviet.ivshs.entities.base.BaseIoTSensor;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "motion_detector",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_motion_detector_natural_id",
          columnNames = {"natural_id"}),
      @UniqueConstraint(
          name = "uq_motion_detector_room_code",
          columnNames = {"room_id", "code"})
    },
    indexes = {
      @Index(name = "idx_motion_detector_room_id", columnList = "room_id"),
      @Index(name = "idx_motion_detector_natural_id", columnList = "natural_id")
    })
@Getter
@Setter
@NoArgsConstructor
public class MotionDetector extends BaseIoTSensor<MotionDetectorLan> {

  private static final long serialVersionUID = 1L;

  @Column(name = "current_motion")
  private Boolean currentMotion;

  @Column(name = "last_event_at")
  private Instant lastEventAt;

  @Override
  public MotionDetectorData extractBusinessData() {
    return new MotionDetectorData(this.currentMotion);
  }

  @Override
  public DeviceCategory getCategory() {
    return DeviceCategory.MOTION_DETECTOR;
  }
}
