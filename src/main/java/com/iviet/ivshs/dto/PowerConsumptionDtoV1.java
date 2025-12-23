package com.iviet.ivshs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PowerConsumptionDtoV1 {
    
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private Double currentWatt;
    private Double currentWattHour;
    private String naturalId;
    private Long roomId;
}
