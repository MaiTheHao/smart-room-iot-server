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
public class SumPowerConsumptionValueDto {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private Instant timestamp;
    private Double sumWatt;
    
    public SumPowerConsumptionValueDto(String formattedTimestamp, Double sumWatt) {
        Instant parsedTime = TimeUtil.parseToInstant(formattedTimestamp, FORMATTER);
        this.timestamp = parsedTime != null ? parsedTime : Instant.now();
        this.sumWatt = sumWatt != null ? sumWatt : 0.0;
    }
}
