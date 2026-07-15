package com.iviet.ivshs.entities;

import com.iviet.ivshs.dto.LuxSensorData;
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
@Table(name = "lux_sensor", indexes = {
        @Index(name = "idx_lux_sensor_room_id", columnList = "room_id", unique = false),
        @Index(name = "idx_lux_sensor_natural_id", columnList = "natural_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class LuxSensor extends BaseIoTSensor<LuxSensorLan> {

    private static final long serialVersionUID = 1L;

    @Column(name = "current_lux")
    private Double currentLux;

    @Override
    public LuxSensorData extractBusinessData() {
        return new LuxSensorData(this.currentLux);
    }

    @Override
    public DeviceCategory getCategory() {
        return DeviceCategory.SENSOR_LUX;
    }
}
