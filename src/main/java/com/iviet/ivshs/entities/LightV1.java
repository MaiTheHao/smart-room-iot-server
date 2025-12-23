package com.iviet.ivshs.entities;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "light_v1",
    indexes = {
        @Index(name = "idx_light_room_id", columnList = "room_id", unique = false),
        @Index(name = "idx_light_natural_id", columnList = "natural_id", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LightV1 extends BaseIoTDeviceV1 {

    private static final long serialVersionUID = 1L;

    @Column(name = "level")
    private Integer level;

    @OneToMany(mappedBy = "light", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<LightLanV1> lightLans = new HashSet<>();
}