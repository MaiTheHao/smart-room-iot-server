package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
public record AirConditionControlRequestBody(
  ActuatorPower power,

  @Min(value = 16, message = "Temperature must be between 16 and 32")
  @Max(value = 32, message = "Temperature must be between 16 and 32")
  Integer temperature,

  ActuatorMode mode,

  @Min(value = 0, message = "Fan speed must be between 0 and 5")
  @Max(value = 5, message = "Fan speed must be between 0 and 5")
  Integer fanSpeed,

  ActuatorSwing swing
) {
}
