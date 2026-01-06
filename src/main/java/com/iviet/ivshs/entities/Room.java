package com.iviet.ivshs.entities;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "room",
    indexes = {
        @Index(name = "idx_room_code", columnList = "code", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room extends BaseTranslatableEntity<RoomLan> {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @Column(name = "code", nullable = false, length = 256)
    private String code;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<Light> lights = new HashSet<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<Temperature> temperatures = new HashSet<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<PowerConsumption> powerConsumptions = new HashSet<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<DeviceControl> deviceControls = new HashSet<>();
}