package com.iviet.ivshs.shared.enumeration;

/**
 * Specific types (variants) within the FAN category.
 *
 * <p>The {@code specificType} only describes the control variant, not the category itself.
 * For example, a GPIO fan has category=FAN and specificType=GPIO.</p>
 *
 * <ul>
 *   <li>{@link #GPIO} — GPIO-controlled fan (simple ON/OFF via Raspberry Pi GPIO pin)</li>
 *   <li>{@link #IRSEND} — IR-controlled fan </li>
 *   <li>{@link #IR_CTL} — IR-controlled fan with duration</li>
 * </ul>
 */
public enum FanType {
    GPIO,
    IRSEND,
    IR_CTL;

    public static FanType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return FanType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
