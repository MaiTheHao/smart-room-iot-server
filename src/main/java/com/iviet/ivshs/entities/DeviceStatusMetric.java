package com.iviet.ivshs.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.base.BaseMetricData;
import com.iviet.ivshs.entities.converter.JsonNodeConverter;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "device_status_metrics", indexes = {
        @Index(name = "idx_device_status_metrics_target", columnList = "target_category, target_id, timestamp"),
        @Index(name = "idx_device_status_metrics_timestamp", columnList = "timestamp"),
        @Index(name = "idx_dsm_unix_minute", columnList = "unix_minute")
})
@Immutable
@Getter
@Setter
@NoArgsConstructor
public class DeviceStatusMetric extends BaseMetricData {

    @Column(name = "status_data", columnDefinition = "TEXT")
    @Convert(converter = JsonNodeConverter.class)
    private JsonNode statusData;

    @Override
    public void setTargetCategory(String targetCategory) {
        if (targetCategory == null || targetCategory.isBlank())
            throw new IllegalArgumentException("Target category cannot be null or blank");
        try {
            DeviceCategory.valueOf(targetCategory);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid target category: " + targetCategory);
        }
        this.targetCategory = targetCategory;
    }
}
