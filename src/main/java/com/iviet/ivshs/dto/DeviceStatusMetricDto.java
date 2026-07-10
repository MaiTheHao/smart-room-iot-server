package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import java.time.Instant;

@Value
@Builder
@Jacksonized
public class DeviceStatusMetricDto {
    Instant timestamp;
    String targetCategory;
    Long targetId;
    JsonNode statusData;
}
