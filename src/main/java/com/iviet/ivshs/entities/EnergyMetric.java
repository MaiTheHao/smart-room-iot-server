package com.iviet.ivshs.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Table(name = "energy_metrics", indexes = {
    @Index(name = "idx_energy_metrics_target", columnList = "category, target_id, timestamp"),
    @Index(name = "idx_energy_metrics_timestamp", columnList = "timestamp")
})
@Immutable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergyMetric extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

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
}
