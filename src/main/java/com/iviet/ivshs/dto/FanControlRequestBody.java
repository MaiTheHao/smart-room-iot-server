package com.iviet.ivshs.dto;

import com.iviet.ivshs.shared.enumeration.ActuatorMode;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.enumeration.ActuatorSwing;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
public record FanControlRequestBody(

		ActuatorPower power,

		ActuatorMode mode,

		@Min(value = 1, message = "Speed must be between 1 and 3")
		@Max(value = 3, message = "Speed must be between 1 and 3")
		Integer speed,

		ActuatorSwing swing) {
}
