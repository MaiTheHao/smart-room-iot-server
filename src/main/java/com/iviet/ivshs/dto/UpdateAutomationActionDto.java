package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.JobActionType;
import com.iviet.ivshs.enumeration.JobTargetType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAutomationActionDto {

    @NotNull(message = "Target type is required")
    private JobTargetType targetType;

    @NotNull(message = "Target ID is required")
    private Long targetId;

    @NotNull(message = "Action type is required")
    private JobActionType actionType;

    private String parameterValue;

    private Integer executionOrder;
}
