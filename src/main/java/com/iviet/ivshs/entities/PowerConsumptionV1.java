package com.iviet.ivshs.entities;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "power_consumption_v1",
    indexes = {
        @Index(name = "idx_power_consumption_room_id", columnList = "room_id", unique = false),
        @Index(name = "idx_power_consumption_natural_id", columnList = "natural_id", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PowerConsumptionV1 extends BaseIoTDevice<PowerConsumptionLanV1> {

    private static final long serialVersionUID = 1L;

    @Column(name = "current_watt")
    private Double currentWatt;

    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<PowerConsumptionValueV1> consumptionValues = new HashSet<>();
}