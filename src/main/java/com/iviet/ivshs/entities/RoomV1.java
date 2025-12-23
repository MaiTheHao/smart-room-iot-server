package com.iviet.ivshs.entities;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
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
@Table(name = "room_v1",
    indexes = {
        @Index(name = "idx_room_code", columnList = "code", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomV1 extends BaseAuditEntityV1 {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    private FloorV1 floor;

    @Column(name = "code", nullable = false, length = 256)
    private String code;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<RoomLanV1> roomLans = new HashSet<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<LightV1> lights = new HashSet<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<TemperatureV1> temperatures = new HashSet<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<PowerConsumptionV1> powerConsumptions = new HashSet<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<DeviceControlV1> deviceControls = new HashSet<>();
}