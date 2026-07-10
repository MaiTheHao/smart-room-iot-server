package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iviet.ivshs.shared.enumeration.ActuatorMode;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.enumeration.ActuatorSwing;

@JsonInclude(Include.NON_NULL)
public record AirConditionData(
    ActuatorPower power, Integer temperature, ActuatorMode mode,
    Integer fanSpeed, ActuatorSwing swing, Integer duration
) implements DeviceSpecificData {
}
