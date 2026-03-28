package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.ActuatorPower;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PowerControlRequest (
  @NotNull(message = "Power state is required")
  ActuatorPower power
) {
}
