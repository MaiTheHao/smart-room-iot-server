package com.iviet.ivshs.shared.enumeration;

public enum ClientType {
    USER,
    HARDWARE_GATEWAY,         // Raspberry Pi
    HARDWARE_GATEWAY_ESP32;   // ESP32

    public boolean isGateway() {
        return this == HARDWARE_GATEWAY || this == HARDWARE_GATEWAY_ESP32;
    }
}
