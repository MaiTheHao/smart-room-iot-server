package com.iviet.ivshs.enumeration;

public enum RuleDataSource {
    SYSTEM("SYSTEM"),
    ROOM("ROOM"),
    DEVICE("DEVICE"),
    SENSOR("SENSOR");

    private final String value;

    RuleDataSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RuleDataSource fromValue(String value) {
        for (RuleDataSource source : RuleDataSource.values()) {
            if (source.value.equalsIgnoreCase(value)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown RuleDataSource: " + value);
    }
}
