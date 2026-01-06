package com.iviet.ivshs.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "floor_lan_v1", 
    indexes = {
        @Index(name = "idx_floor_lan_owner_id_lang_code", columnList = "owner_id, lang_code", unique = true)
    }
)
public class FloorLanV1 extends BaseTranslation<FloorV1> {
}