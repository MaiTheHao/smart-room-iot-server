package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Automation;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAutomationDto {

    @NotBlank(message = "Automation name is required")
    private String name;

    @NotBlank(message = "Cron expression is required")
    private String cronExpression;

    private Boolean isActive = true;

    private String description;

    public static Automation toEntity(CreateAutomationDto dto) {
        Automation automation = new Automation();
        automation.setName(dto.getName());
        automation.setCronExpression(dto.getCronExpression());
        automation.setIsActive(dto.getIsActive());
        automation.setDescription(dto.getDescription());
        return automation;
    }
}
