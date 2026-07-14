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
@Table(name = "humidity_metrics", indexes = {
        @Index(name = "idx_humid_metrics_target", columnList = "target_category, target_id, timestamp"),
        @Index(name = "idx_humid_metrics_timestamp", columnList = "timestamp"),
        @Index(name = "idx_hm_unix_minute", columnList = "unix_minute")
})
@Immutable
@Getter
@Setter
@NoArgsConstructor
public class HumidityMetric extends BaseMetricData {

    @Column(name = "humidity", nullable = false)
    private Double humidity;

    @Override
    public void setTargetCategory(String targetCategory) {
        if (targetCategory == null || targetCategory.isBlank())
            throw new IllegalArgumentException("Target category cannot be null or blank");
        if (!"HUMIDITY".equals(targetCategory) && !"SENSOR".equals(targetCategory)) {
            throw new IllegalArgumentException("Invalid target category: " + targetCategory);
        }
        this.targetCategory = targetCategory;
    }
}
