package com.iviet.ivshs.enumeration;

import lombok.Getter;

@Getter
public enum EnergyMetricCategory {
    LIGHT("lights"),
    AIR_CONDITION("air-conditions"),
    FAN("fans"),
    ROOM("power-consumptions");

    private final String domain;

    EnergyMetricCategory(String domain) {
        this.domain = domain;
    }
}
