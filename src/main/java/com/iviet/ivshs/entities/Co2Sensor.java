package com.iviet.ivshs.entities;

import com.iviet.ivshs.dto.Co2SensorData;
import com.iviet.ivshs.entities.base.BaseIoTSensor;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "co2_sensor", indexes = {
        @Index(name = "idx_co2_sensor_room_id", columnList = "room_id", unique = false),
        @Index(name = "idx_co2_sensor_natural_id", columnList = "natural_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class Co2Sensor extends BaseIoTSensor<Co2SensorLan> {

    private static final long serialVersionUID = 1L;

    @Column(name = "current_co2")
    private Double currentCO2;

    @Override
    public Co2SensorData extractBusinessData() {
        return new Co2SensorData(this.currentCO2);
    }

    @Override
    public DeviceCategory getCategory() {
        return DeviceCategory.SENSOR_CO2;
    }
}
