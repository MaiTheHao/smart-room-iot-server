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
public class PowerConsumptionValueDto {
    
    private Long id;
    private Long sensorId;
    private Double watt;
    private Instant timestamp;
}
