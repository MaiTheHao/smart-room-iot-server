package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.entities.DeviceStatusMetric;
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

    public static JsonNode businessDataToJsonNode(Object businessData, ObjectMapper mapper) {
        return mapper.valueToTree(businessData);
    }

    public static DeviceStatusMetricDto fromEntity(DeviceStatusMetric entity) {
        return DeviceStatusMetricDto.builder()
                .timestamp(entity.getTimestamp())
                .targetCategory(entity.getTargetCategory())
                .targetId(entity.getTargetId())
                .statusData(entity.getStatusData())
                .build();
    }
}
