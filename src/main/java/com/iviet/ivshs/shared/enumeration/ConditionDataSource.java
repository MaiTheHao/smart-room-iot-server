package com.iviet.ivshs.shared.enumeration;

public enum ConditionDataSource {
    SYSTEM("SYSTEM"),
    ROOM("ROOM"),
    DEVICE("DEVICE"),
    SENSOR("SENSOR");

    private final String value;

    ConditionDataSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ConditionDataSource fromValue(String value) {
        for (ConditionDataSource source : ConditionDataSource.values()) {
            if (source.value.equalsIgnoreCase(value)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown ConditionDataSource: " + value);
    }
}
