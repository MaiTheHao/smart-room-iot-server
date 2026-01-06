package com.iviet.ivshs.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "light_lan_v1",
    indexes = {
        @Index(name = "idx_light_lan_owner_id_lang_code", columnList = "owner_id, lang_code", unique = true)
    }
)
public class LightLanV1 extends BaseTranslation<LightV1> {
}