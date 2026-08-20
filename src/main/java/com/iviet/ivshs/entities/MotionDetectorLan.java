package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.base.BaseTranslation;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "motion_detector_lan",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_mdl_owner_lang",
          columnNames = {"owner_id", "lang_code"})
    },
    indexes = {@Index(name = "idx_mdl_owner_id", columnList = "owner_id")})
@Getter
@Setter
@NoArgsConstructor
public class MotionDetectorLan extends BaseTranslation<MotionDetector> {

  private static final long serialVersionUID = 1L;
}
