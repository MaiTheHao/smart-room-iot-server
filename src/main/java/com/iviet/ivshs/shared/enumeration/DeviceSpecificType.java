package com.iviet.ivshs.shared.enumeration;

/**
 * Unified hardware control variant type, shared across all IoT device categories.
 * <p>
 * The {@code specificType} describes how the gateway controls the physical device, independent of the device category (Fan, Light, AirCondition, etc.).
 * </p>
 * <ul>
 * <li>{@link #GPIO} — Simple ON/OFF via GPIO pin</li>
 * <li>{@link #IRSEND} — IR signal (stateless, no duration)</li>
 * <li>{@link #IR_CTL} — IR signal with timed duration</li>
 * </ul>
 */
public enum DeviceSpecificType {
    GPIO, IRSEND, IR_CTL;

    public static DeviceSpecificType fromString(String value) {
        if (value == null || value.isBlank())
            return null;
        try {
            return DeviceSpecificType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
