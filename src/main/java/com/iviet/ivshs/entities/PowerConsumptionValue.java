package com.iviet.ivshs.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "power_consumption_value", indexes = {
    @Index(name = "idx_sensor_timestamp", columnList = "sensor_id, timestamp"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Immutable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PowerConsumptionValue extends BaseTelemetryValue<PowerConsumption> {

    private static final long serialVersionUID = 1L;

    @Column(name = "watt")
    private Double watt;
}