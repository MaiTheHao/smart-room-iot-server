package com.iviet.ivshs.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum GatewayCommand {

    ON("TRUE"),
    OFF("FALSE");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }
	
    @JsonCreator
    public static GatewayCommand fromValue(String value) {
        return Arrays.stream(values())
                .filter(v -> v.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unknown GatewayCommandV1: " + value)
                );
    }

    public static GatewayCommand fromBoolean(boolean state) {
        return state ? ON : OFF;
    }
}
