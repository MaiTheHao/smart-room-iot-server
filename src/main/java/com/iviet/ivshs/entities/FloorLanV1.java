package com.iviet.ivshs.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "floor_lan_v1", 
    indexes = {
        @Index(name = "idx_floor_id_floor_lang_code", columnList = "floor_id, lang_code", unique = true)
    }
)
public class FloorLanV1 extends BaseTranslationV1<FloorV1> {
}