package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.base.BaseTranslation;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "co2_sensor_lan", indexes = {
        @Index(name = "idx_co2_sensor_lan_owner_id_lang_code", columnList = "owner_id, lang_code", unique = true)
})
public class Co2SensorLan extends BaseTranslation<Co2Sensor> {
}
