package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.enumeration.ActuatorPower;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateAirConditionDto(
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    String name,

    String naturalId,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description,

    Boolean isActive,

    Long roomId,

    Long deviceControlId,
    
    String langCode,

    ActuatorPower power,

    @Min(value = 16, message = "Temperature must be between 16 and 32")
    @Max(value = 32, message = "Temperature must be between 16 and 32")
    Integer temperature,

    ActuatorMode mode,

    @Min(value = 0, message = "Fan speed must be between 0 and 5")
    @Max(value = 5, message = "Fan speed must be between 0 and 5")
    Integer fanSpeed,

    ActuatorSwing swing
) {}