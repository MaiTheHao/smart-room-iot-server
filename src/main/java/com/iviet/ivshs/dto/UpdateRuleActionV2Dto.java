package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.enumeration.DeviceCategory;
import lombok.Builder;

@Builder
public record UpdateRuleActionV2Dto(
  Long id,

  Long targetDeviceId,

  DeviceCategory targetDeviceCategory,

  JsonNode actionParams,

  Integer executionOrder
) {}