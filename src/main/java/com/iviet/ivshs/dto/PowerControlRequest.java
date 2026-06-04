package com.iviet.ivshs.dto;

import com.iviet.ivshs.shared.enumeration.ActuatorPower;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PowerControlRequest(
    @NotNull(message = "Power state is required") ActuatorPower power) {
}
