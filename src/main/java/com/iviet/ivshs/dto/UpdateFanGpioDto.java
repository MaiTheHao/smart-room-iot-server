package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.FanType;

import jakarta.validation.constraints.Size;

public record UpdateFanGpioDto(
    @Size(min = 1, max = 100)
    String name,

    String naturalId,

    @Size(max = 255)
    String description,

    Boolean isActive,

    Long roomId,

    Long deviceControlId,

    @Size(max = 10)
    String langCode,

    ActuatorPower power,

    FanType type
) implements UpdateFanDto {}
