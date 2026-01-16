package com.iviet.ivshs.dto;

import java.util.List;

import com.iviet.ivshs.enumeration.JobActionType;
import com.iviet.ivshs.enumeration.JobTargetType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @NotEmpty(message = "Actions list must not be empty")
    @Valid
    private List<AutomationActionDto> actions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AutomationActionDto {

        @NotNull(message = "Target type must not be null")
        private JobTargetType targetType;

        @NotNull(message = "Target ID must not be null")
        private Long targetId;

        @NotNull(message = "Action type must not be null")
        private JobActionType actionType;

        private String parameterValue;

        private Integer executionOrder = 0;
    }
}
