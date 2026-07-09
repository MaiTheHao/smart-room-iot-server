package com.iviet.ivshs.dto;

import com.iviet.ivshs.shared.enumeration.DeviceSpecificType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable value object that encapsulates device hardware control metadata needed to build a gateway request payload.
 *
 * <p>
 * The payload rules are:
 * </p>
 * <ul>
 * <li>All types: always include {@code specificType} in the body (if non-null).</li>
 * <li>{@code IR_CTL} only: also include {@code duration} if non-null.</li>
 * </ul>
 */
public record DeviceControlPayload(DeviceSpecificType type, Integer duration, Object data) {
    private static final String DATA = "data";
    private static final String SPECIFIC_TYPE = "specificType";
    private static final String DURATION = "duration";

    public static DeviceControlPayload of(DeviceSpecificType type, Object data) {
        return new DeviceControlPayload(type, null, data);
    }

    public static DeviceControlPayload of(DeviceSpecificType type, Integer duration, Object data) {
        return new DeviceControlPayload(type, duration, data);
    }

    public Map<String, Object> toMap() {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put(DATA, data);
        if (type != null) {
            body.put(SPECIFIC_TYPE, type.name());
            if (type == DeviceSpecificType.IR_CTL && duration != null) {
                body.put(DURATION, duration);
            }
        }
        return body;
    }
}
