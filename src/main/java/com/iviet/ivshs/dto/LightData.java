package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;

@JsonInclude(Include.NON_NULL)
public record LightData(ActuatorPower power, Integer level) implements DeviceSpecificData {
}
