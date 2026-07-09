package com.iviet.ivshs.dto;

import com.iviet.ivshs.shared.enumeration.ActuatorMode;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.enumeration.ActuatorSwing;
import com.iviet.ivshs.shared.enumeration.DeviceSpecificType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateFanDto(
        @Size(min = 1, max = 100) String name,

        String naturalId,

        @Size(max = 255) String description,

        Boolean isActive,

        Long roomId,

        Long deviceControlId,

        @Size(max = 10) String langCode,

        ActuatorPower power,

        DeviceSpecificType type,

        @Min(value = 1, message = "Speed must be at least 1") @Max(value = 5, message = "Speed must be at most 5") Integer speed,

        ActuatorMode mode,

        ActuatorSwing swing,

        ActuatorPower light) {
}
