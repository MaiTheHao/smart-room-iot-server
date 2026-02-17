package com.iviet.ivshs.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "light",
    indexes = {
        @Index(name = "idx_light_room_id", columnList = "room_id", unique = false),
        @Index(name = "idx_light_natural_id", columnList = "natural_id", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Light extends BaseIoTActuator<LightLan>{
    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 100;

    @Column(name = "level")
    private Integer level;
}