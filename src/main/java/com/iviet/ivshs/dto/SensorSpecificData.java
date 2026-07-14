package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(TemperatureSensorData.class),
    @JsonSubTypes.Type(PowerConsumptionSensorData.class),
    @JsonSubTypes.Type(HumiditySensorData.class)
})
public sealed interface SensorSpecificData
    permits TemperatureSensorData, PowerConsumptionSensorData, HumiditySensorData {
}
