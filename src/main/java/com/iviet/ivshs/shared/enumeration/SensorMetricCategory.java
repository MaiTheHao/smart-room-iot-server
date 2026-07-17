package com.iviet.ivshs.shared.enumeration;

import com.iviet.ivshs.shared.exception.BadRequestException;

public enum SensorMetricCategory {
    DEFAULT, ROOM;

    /**
     * null / blank → DEFAULT
     * "room" / "ROOM" → ROOM
     * anything else  → BadRequestException
     */
    public static SensorMetricCategory fromString(String category) {
        if (category == null || category.isBlank()) return DEFAULT;
        try {
            return valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                "Invalid category '" + category + "' for sensor metrics. Accepted values: [DEFAULT, ROOM]");
        }
    }
}
