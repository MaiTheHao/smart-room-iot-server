package com.iviet.ivshs.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "fan_lan",
    indexes = {
        @Index(name = "idx_fan_lan_owner_id_lang_code", columnList = "owner_id, lang_code", unique = true)
    }
)
public class FanLan extends BaseTranslation<Fan> {
}
