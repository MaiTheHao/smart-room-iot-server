package com.iviet.ivshs.entities;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fan",
    indexes = {
        @Index(name = "idx_fan_room_id", columnList = "room_id", unique = false),
        @Index(name = "idx_fan_natural_id", columnList = "natural_id", unique = true)
    }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class Fan extends BaseIoTActuator<FanLan> {
    
    @Override 
    public void addTranslation(FanLan translation) {
        translation.setOwner(this);
        this.getTranslations().add(translation);
    }
}
