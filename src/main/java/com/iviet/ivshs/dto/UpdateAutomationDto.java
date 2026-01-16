package com.iviet.ivshs.dto;

import java.util.List;

import com.iviet.ivshs.enumeration.JobActionType;
import com.iviet.ivshs.enumeration.JobTargetType;

import jakarta.validation.Valid;
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

    @Valid
    private List<AutomationActionDto> actions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AutomationActionDto {

        private JobTargetType targetType;

        private Long targetId;

        private JobActionType actionType;

        private String parameterValue;

        private Integer executionOrder = 0;
    }
}
