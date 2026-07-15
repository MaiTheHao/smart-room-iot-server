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
@Table(name = "co2_metrics", indexes = {
        @Index(name = "idx_co2_metrics_target", columnList = "target_category, target_id, timestamp"),
        @Index(name = "idx_co2_metrics_timestamp", columnList = "timestamp"),
        @Index(name = "idx_co2m_unix_minute", columnList = "unix_minute")
})
@Immutable
@Getter
@Setter
@NoArgsConstructor
public class Co2Metric extends BaseMetricData {

    @Column(name = "co2", nullable = false)
    private Double co2;

    @Override
    public void setTargetCategory(String targetCategory) {
        if (targetCategory == null || targetCategory.isBlank())
            throw new IllegalArgumentException("Target category cannot be null or blank");
        if (!"SENSOR_CO2".equals(targetCategory)) {
            throw new IllegalArgumentException("Invalid target category: " + targetCategory);
        }
        this.targetCategory = targetCategory;
    }
}
