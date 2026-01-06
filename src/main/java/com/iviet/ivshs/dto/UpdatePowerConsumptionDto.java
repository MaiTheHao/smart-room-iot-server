package com.iviet.ivshs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePowerConsumptionDto {
    
    @Size(min = 1, max = 100, message = "Power consumption sensor name must be between 1 and 100 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private Boolean isActive;

    @Size(max = 100, message = "Natural ID must not exceed 100 characters")
    private String naturalId;

    private Long deviceControlId;

    @Size(max = 10, message = "Language code must not exceed 10 characters")
    private String langCode;
}
