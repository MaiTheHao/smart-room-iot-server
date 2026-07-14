package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iviet.ivshs.entities.HumidityMetric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Getter
@Setter
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HumidityMetricDto {

    private Instant timestamp;
    private Double humidity;

    public static HumidityMetricDto fromEntity(HumidityMetric entity) {
        return HumidityMetricDto.builder()
                .timestamp(entity.getTimestamp())
                .humidity(entity.getHumidity())
                .build();
    }
}
