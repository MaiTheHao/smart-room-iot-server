package com.iviet.ivshs.dto;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import com.iviet.ivshs.util.TimeUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SumPowerConsumptionValueDtoV1 {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private Instant timestamp;
    private Double sumWatt;
    private Double sumWattHour;
    
    public SumPowerConsumptionValueDtoV1(String formattedTimestamp, Double sumWatt, Double sumWattHour) {
        Instant parsedTime = TimeUtil.parseToInstant(formattedTimestamp, FORMATTER);
        this.timestamp = parsedTime != null ? parsedTime : Instant.now();
        this.sumWatt = sumWatt != null ? sumWatt : 0.0;
        this.sumWattHour = sumWattHour != null ? sumWattHour : 0.0;
    }
}
