package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iviet.ivshs.entities.LuxMetric;
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
public class LuxMetricDto {

    private Instant timestamp;
    private Double lux;

    public static LuxMetricDto fromEntity(LuxMetric entity) {
        return LuxMetricDto.builder()
                .timestamp(entity.getTimestamp())
                .lux(entity.getLux())
                .build();
    }
}
