package com.iviet.ivshs.entities;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "temperature_v1",
    indexes = {
        @Index(name = "idx_temperature_room_id", columnList = "room_id", unique = false),
        @Index(name = "idx_temperature_natural_id", columnList = "natural_id", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemperatureV1 extends BaseIoTDeviceV1 {

    private static final long serialVersionUID = 1L;

    @Column(name = "current_value")
    private Double currentValue;

    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TemperatureLanV1> sensorLans = new HashSet<>();

    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TemperatureValueV1> temperatureValues = new HashSet<>();
}