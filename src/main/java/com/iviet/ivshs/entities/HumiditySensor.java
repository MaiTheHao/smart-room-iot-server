package com.iviet.ivshs.entities;

import com.iviet.ivshs.dto.HumiditySensorData;
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
@Table(name = "humidity_sensor", indexes = {
        @Index(name = "idx_humidity_sensor_room_id", columnList = "room_id", unique = false),
        @Index(name = "idx_humidity_sensor_natural_id", columnList = "natural_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class HumiditySensor extends BaseIoTSensor<HumiditySensorLan> {

    private static final long serialVersionUID = 1L;

    @Column(name = "current_humidity")
    private Double currentHumidity;

    @Override
    public HumiditySensorData extractBusinessData() {
        return new HumiditySensorData(this.currentHumidity);
    }

    @Override
    public DeviceCategory getCategory() {
        return DeviceCategory.HUMIDITY;
    }

}