package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.iviet.ivshs.entities.EnergyMetric;

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

    public EnergyMetric toEntity(String targetCategory, Long targetId) {
        EnergyMetric metric = new EnergyMetric();
        metric.setTargetCategory(targetCategory);
        metric.setTargetId(targetId);
        metric.setTimestamp(this.timestamp);
        metric.setVoltage(this.voltage);
        metric.setCurrent(this.current);
        metric.setPower(this.power);
        metric.setEnergy(this.energy);
        metric.setFrequency(this.frequency);
        metric.setPowerFactor(this.powerFactor);
        return metric;
    }
}
