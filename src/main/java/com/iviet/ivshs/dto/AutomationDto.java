package com.iviet.ivshs.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.iviet.ivshs.enumeration.JobActionType;
import com.iviet.ivshs.enumeration.JobTargetType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutomationDto {

    private Long id;
    private String name;
    private String cronExpression;
    private Boolean isActive;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AutomationActionDto> actions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AutomationActionDto {
        private Long id;
        private JobTargetType targetType;
        private Long targetId;
        private JobActionType actionType;
        private String parameterValue;
        private Integer executionOrder;
        private String targetName;
    }
}
