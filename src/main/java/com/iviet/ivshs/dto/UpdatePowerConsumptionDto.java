package com.iviet.ivshs.dto;

import lombok.Builder;

import jakarta.validation.constraints.Size;

@Builder
public record UpdatePowerConsumptionDto(
    @Size(min = 1, max = 100, message = "Power consumption sensor name must be between 1 and 100 characters")
    String name,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description,

    Boolean isActive,

    @Size(max = 100, message = "Natural ID must not exceed 100 characters")
    String naturalId,

    Long deviceControlId,

    @Size(max = 10, message = "Language code must not exceed 10 characters")
    String langCode
) {}
