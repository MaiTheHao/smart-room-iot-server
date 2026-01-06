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

import com.iviet.ivshs.enumeration.DeviceControlType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "device_control", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"client_id", "gpio_pin", "ble_mac_address"}),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceControl extends BaseAuditEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "device_control_type", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceControlType deviceControlType;

    @Column(name = "gpio_pin", nullable = false)
    private Integer gpioPin;

    @Column(name = "ble_mac_address", length = 100, nullable = true)
    private String bleMacAddress;

    @Column(name = "api_endpoint", length = 255, nullable = true)
    private String apiEndpoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToOne(mappedBy = "deviceControl", fetch = FetchType.LAZY)
    private Light light;

    @OneToOne(mappedBy = "deviceControl", fetch = FetchType.LAZY)
    private Temperature temperature;

    @OneToOne(mappedBy = "deviceControl", fetch = FetchType.LAZY)
    private PowerConsumption powerConsumption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
}