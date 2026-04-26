package com.iviet.ivshs.entities;

import com.iviet.ivshs.enumeration.EnergyMetricCategory;
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
    name = "energy_metrics",
    indexes = {
        @Index(name = "idx_energy_metrics_target", columnList = "target_category, target_id, timestamp"),
        @Index(name = "idx_energy_metrics_timestamp", columnList = "timestamp"),
        @Index(name = "idx_em_unix_minute", columnList = "unix_minute")
    }
)
@Immutable
@Getter
@Setter
@NoArgsConstructor
public class EnergyMetric extends BaseMetricData {

    @Column(name = "voltage")
    private Double voltage;

    @Column(name = "current")
    private Double current;

    @Column(name = "power")
    private Double power;

    @Column(name = "energy")
    private Double energy;

    @Column(name = "frequency")
    private Double frequency;

    @Column(name = "power_factor")
    private Double powerFactor;

    @Override
    public void setTargetCategory(String targetCategory) {
        if (targetCategory == null || targetCategory.isBlank()) throw new IllegalArgumentException("Target category cannot be null or blank");
        try {
            EnergyMetricCategory.valueOf(targetCategory);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid target category: " + targetCategory);
        }
        this.targetCategory = targetCategory;
    }
}