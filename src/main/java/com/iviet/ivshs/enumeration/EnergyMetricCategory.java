package com.iviet.ivshs.enumeration;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.iviet.ivshs.exception.domain.BadRequestException;

public enum EnergyMetricCategory {
    LIGHT,
    AIR_CONDITION,
    FAN,
    ROOM;

    public static EnergyMetricCategory fromString(String category) {
        if (category == null || category.isBlank()) throw new BadRequestException("Category is required for energy metrics.");
        
        String upper = category.toUpperCase();
        try {
            return EnergyMetricCategory.valueOf(upper);
        } catch (IllegalArgumentException e) {
            String accepted = Arrays.stream(EnergyMetricCategory.values())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            throw new BadRequestException(String.format("Invalid category '%s' for energy metrics. Accepted values: [%s]", category, accepted));
        }
    }
}
