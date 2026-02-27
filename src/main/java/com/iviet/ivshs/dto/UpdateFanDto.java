package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.FanType;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = UpdateFanIrDto.class, name = "IR"),
    @JsonSubTypes.Type(value = UpdateFanGpioDto.class, name = "GPIO")
})
public sealed interface UpdateFanDto permits UpdateFanIrDto, UpdateFanGpioDto {
    String name();
    String naturalId();
    String description();
    Boolean isActive();
    Long roomId();
    Long deviceControlId();
    String langCode();
    ActuatorPower power();
    FanType type();
}
