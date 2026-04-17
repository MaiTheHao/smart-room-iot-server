package com.iviet.ivshs.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SumPowerConsumptionValueDto {
    private Instant timestamp;
    private Double sumWatt;

    public SumPowerConsumptionValueDto(Long unixSeconds, Double sumWatt) {
        this.timestamp = Instant.ofEpochSecond(unixSeconds);
        this.sumWatt = sumWatt;
    }
}
