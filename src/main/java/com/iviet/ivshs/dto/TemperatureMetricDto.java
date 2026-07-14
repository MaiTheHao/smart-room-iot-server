package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iviet.ivshs.entities.TemperatureMetric;
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
public class TemperatureMetricDto {

    private Instant timestamp;
    private Double temperature;

    public static TemperatureMetricDto fromEntity(TemperatureMetric entity) {
        return TemperatureMetricDto.builder()
                .timestamp(entity.getTimestamp())
                .temperature(entity.getTemperature())
                .build();
    }
}
