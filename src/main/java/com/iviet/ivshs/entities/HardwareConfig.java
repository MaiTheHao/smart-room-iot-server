package com.iviet.ivshs.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.iviet.ivshs.enumeration.DeviceControlType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hardware_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HardwareConfig extends BaseAuditEntity {

    private static final long serialVersionUID = 1L;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "control_type", length = 256, nullable = false)
    private DeviceControlType controlType;

    @Column(name = "gpio_pin", nullable = false)
    private Integer gpioPin;

    @Column(name = "ble_mac_address", length = 100, nullable = true)
    private String bleMacAddress;

    @Column(name = "api_endpoint", length = 255, nullable = true)
    private String apiEndpoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToOne(mappedBy = "hardwareConfig", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Light light;

    @OneToOne(mappedBy = "hardwareConfig", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Temperature temperature;

    @OneToOne(mappedBy = "hardwareConfig", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private PowerConsumption powerConsumption;

    @OneToOne(mappedBy = "hardwareConfig", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Fan fan;

    @OneToOne(mappedBy = "hardwareConfig", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private AirCondition airCondition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
}