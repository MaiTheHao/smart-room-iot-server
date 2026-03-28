package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.enumeration.FanType;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CreateFanIrDto.class, name = "IR"),
    @JsonSubTypes.Type(value = CreateFanGpioDto.class, name = "GPIO")
})
public sealed interface CreateFanDto permits CreateFanIrDto, CreateFanGpioDto {
    String name();
    String naturalId();
    String description();
    Boolean isActive();
    Long roomId();
    Long deviceControlId();
    String langCode();
    com.iviet.ivshs.enumeration.ActuatorPower power();
    FanType type();
    Fan toEntity();
}
