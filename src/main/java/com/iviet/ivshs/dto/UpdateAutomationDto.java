package com.iviet.ivshs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAutomationDto {

    private String name;

    private String cronExpression;

    private Boolean isActive;

    private String description;
}
