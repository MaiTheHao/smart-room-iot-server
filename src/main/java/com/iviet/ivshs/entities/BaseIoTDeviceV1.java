package com.iviet.ivshs.entities;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseIoTDeviceV1 extends BaseAuditEntityV1 {

    private static final long serialVersionUID = 1L;

    @Column(name = "natural_id", length = 256, unique = true, nullable = false)
    private String naturalId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomV1 room;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_control_id", unique = true)
    private DeviceControlV1 deviceControl;
}
