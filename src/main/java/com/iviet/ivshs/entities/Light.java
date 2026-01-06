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
public class Light extends BaseIoTDevice<LightLan> {

    private static final long serialVersionUID = 1L;

    @Column(name = "level")
    private Integer level;
}