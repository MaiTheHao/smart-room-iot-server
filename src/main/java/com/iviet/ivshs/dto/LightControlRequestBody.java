package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.ActuatorPower;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record LightControlRequestBody(
    ActuatorPower power,
    
    @Min(value = 0, message = "Level must be between 0 and 100")
    @Max(value = 100, message = "Level must be between 0 and 100")
    Integer level
) {}
