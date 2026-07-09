package com.iviet.ivshs.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAutomationDto {

    @NotBlank(message = "Automation name is required")
    private String name;

    @NotBlank(message = "Cron expression is required")
    private String cronExpression;

    private Boolean isActive;

    private String description;
}

