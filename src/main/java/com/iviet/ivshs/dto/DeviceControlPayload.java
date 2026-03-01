package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
  property = "targetDeviceCategory"
)
@JsonSubTypes({
  @JsonSubTypes.Type(value = FanControlRequestBody.class, name = "FAN"),
  @JsonSubTypes.Type(value = LightControlRequestBody.class, name = "LIGHT"),
  @JsonSubTypes.Type(value = AirConditionControlRequestBody.class, name = "AIR_CONDITION")
})
public interface DeviceControlPayload {}
