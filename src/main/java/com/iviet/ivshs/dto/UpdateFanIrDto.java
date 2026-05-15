package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.enumeration.FanType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateFanIrDto(
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

    FanType type,

    ActuatorMode mode,

    @Min(value = 1, message = "Speed must be at least 1")
    @Max(value = 3, message = "Speed must be at most 3")
    Integer speed,

    ActuatorSwing swing,

    ActuatorPower light
) implements UpdateFanDto {}
