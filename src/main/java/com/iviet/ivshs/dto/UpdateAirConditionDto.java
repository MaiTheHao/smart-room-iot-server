package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.AcMode;
import com.iviet.ivshs.enumeration.AcPower;
import com.iviet.ivshs.enumeration.AcSwing;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateAirConditionDto(
    @Size(min = 1, max = 100)
    String name,

    String naturalId,

    @Size(max = 255)
    String description,

    Boolean isActive,

    Long roomId,
    Long deviceControlId,
    String langCode,

    AcPower power,

    @Min(16) @Max(32)
    Integer temperature,

    AcMode mode,

    @Min(0) @Max(5)
    Integer fanSpeed,

    AcSwing swing
) {}