package com.iviet.ivshs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePowerConsumptionValueDtoV1 {
    
    @NotNull(message = "Watt value is required")
    private Double watt;

    private Double wattHour;

    @NotNull(message = "Timestamp is required")
    private Instant timestamp;
}
