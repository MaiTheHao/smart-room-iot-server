package com.iviet.ivshs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlLightDtoV1 {
    
    @NotNull(message = "isActive is required")
    private Boolean isActive;

    @Min(value = 0, message = "Level must be at least 0")
    @Max(value = 100, message = "Level must not exceed 100")
    private Integer level;
}
