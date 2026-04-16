package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class EnergyMetricDto {

    private Instant timestamp;

    private Double voltage;

    private Double current;

    private Double power;

    private Double energy;

    private Double frequency;

    @JsonProperty("powerFactor")
    private Double powerFactor;
}
