package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(LightData.class),
    @JsonSubTypes.Type(FanData.class),
    @JsonSubTypes.Type(AirConditionData.class)
})
public sealed interface DeviceSpecificData
    permits LightData, FanData, AirConditionData {
}
