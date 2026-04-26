package com.iviet.ivshs.entities;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseIoTSensor<L extends BaseTranslation<? extends BaseTranslatableEntity<L>>> extends BaseIoTEntity<L> {
    
}