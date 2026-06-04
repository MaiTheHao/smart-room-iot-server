package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.base.BaseTranslation;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "temperature_lan", indexes = {
        @Index(name = "idx_temperature_lan_owner_id_lang_code", columnList = "owner_id, lang_code", unique = true)
})
public class TemperatureLan extends BaseTranslation<Temperature> {
}