package com.iviet.ivshs.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "floor_lan", 
    indexes = {
        @Index(name = "idx_floor_lan_owner_id_lang_code", columnList = "owner_id, lang_code", unique = true)
    }
)
public class FloorLan extends BaseTranslation<Floor> {
}