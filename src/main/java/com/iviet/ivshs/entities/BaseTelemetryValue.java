package com.iviet.ivshs.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseTelemetryValue<T extends BaseIoTDevice<?>> extends BaseEntity {

	@Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false, updatable = false)
    private T sensor;
}
