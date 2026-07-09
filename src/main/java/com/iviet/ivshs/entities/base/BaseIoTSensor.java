package com.iviet.ivshs.entities.base;

import com.iviet.ivshs.dto.SensorSpecificData;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseIoTSensor<L extends BaseTranslation<? extends BaseTranslatableEntity<L>>>
    extends BaseIoTEntity<L> {

    abstract public SensorSpecificData extractBusinessData();
}