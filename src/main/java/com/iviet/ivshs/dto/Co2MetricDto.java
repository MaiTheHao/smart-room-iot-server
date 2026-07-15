package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iviet.ivshs.entities.Co2Metric;
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
public class Co2MetricDto {

    private Instant timestamp;
    private Double co2;

    public static Co2MetricDto fromEntity(Co2Metric entity) {
        return Co2MetricDto.builder()
                .timestamp(entity.getTimestamp())
                .co2(entity.getCo2())
                .build();
    }
}
