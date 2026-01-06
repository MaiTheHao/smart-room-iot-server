package com.iviet.ivshs.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.iviet.ivshs.enumeration.DeviceControlTypeV1;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "device_control_v1", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"client_id", "gpio_pin", "ble_mac_address"}),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceControlV1 extends BaseAuditEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "device_control_type", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceControlTypeV1 deviceControlType;

    @Column(name = "gpio_pin", nullable = false)
    private Integer gpioPin;

    @Column(name = "ble_mac_address", length = 100, nullable = true)
    private String bleMacAddress;

    @Column(name = "api_endpoint", length = 255, nullable = true)
    private String apiEndpoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientV1 client;

    @OneToOne(mappedBy = "deviceControl", fetch = FetchType.LAZY)
    private LightV1 light;

    @OneToOne(mappedBy = "deviceControl", fetch = FetchType.LAZY)
    private TemperatureV1 temperature;

    @OneToOne(mappedBy = "deviceControl", fetch = FetchType.LAZY)
    private PowerConsumptionV1 powerConsumption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomV1 room;
}