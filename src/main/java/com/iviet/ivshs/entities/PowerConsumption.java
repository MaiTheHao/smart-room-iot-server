package com.iviet.ivshs.entities;

import com.iviet.ivshs.dto.PowerConsumptionSensorData;
import com.iviet.ivshs.entities.base.BaseIoTSensor;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "power_consumption", indexes = {
        @Index(name = "idx_power_consumption_room_id", columnList = "room_id", unique = false),
        @Index(name = "idx_power_consumption_natural_id", columnList = "natural_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PowerConsumption extends BaseIoTSensor<PowerConsumptionLan> {

    private static final long serialVersionUID = 1L;

    @Column(name = "current_watt")
    private Double currentWatt;

    @Override
    public PowerConsumptionSensorData extractBusinessData() {
        return new PowerConsumptionSensorData(this.currentWatt);
    }

    @Override
    public DeviceCategory getCategory() {
        return DeviceCategory.POWER_CONSUMPTION;
    }

}