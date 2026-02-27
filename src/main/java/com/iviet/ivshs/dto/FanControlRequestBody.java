package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorState;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
public record FanControlRequestBody(
    ActuatorPower power,

    ActuatorMode mode,

    @Min(value = 0, message = "Speed must be between 0 and 9999")
    @Max(value = 9999, message = "Speed must be between 0 and 9999")
    Integer speed,

    ActuatorSwing swing,
    
    ActuatorState light
) {
}
