package com.iviet.ivshs.dto;

import java.util.List;

import com.iviet.ivshs.entities.Rule;
import com.iviet.ivshs.entities.RuleCondition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateRuleDto(
    @NotBlank(message = "Rule name is required")
    String name,

    @NotNull(message = "Priority is required")
    @Min(value = 0, message = "Priority must be at least 0")
    Integer priority,

    @NotNull(message = "Target device ID is required")
    Long targetDeviceId,

    @NotBlank(message = "Target device category is required")
    String targetDeviceCategory,

    String actionParams,

    Boolean isActive,

    @Valid
    @NotNull(message = "At least one condition is required")
    List<UpdateRuleConditionDto> conditions
) {
    public Rule toEntity(Long ruleId) {
        Rule rule = new Rule();
        rule.setId(ruleId);
        rule.setName(this.name);
        rule.setPriority(this.priority);
        rule.setTargetDeviceId(this.targetDeviceId);
        rule.setTargetDeviceCategory(this.targetDeviceCategory);
        rule.setActionParams(this.actionParams);
        
        if (this.isActive != null) {
            rule.setIsActive(this.isActive);
        }

        if (this.conditions != null && !this.conditions.isEmpty()) {
            for (UpdateRuleConditionDto condDto : this.conditions) {
                RuleCondition condition = new RuleCondition();
                if (condDto.id() != null) {
                    condition.setId(condDto.id());
                }
                condition.setSortOrder(condDto.sortOrder());
                condition.setDataSource(condDto.dataSource());
                condition.setResourceParam(condDto.resourceParam());
                condition.setOperator(condDto.operator());
                condition.setValue(condDto.value());
                condition.setNextLogic(condDto.nextLogic());
                rule.addCondition(condition);
            }
        }

        return rule;
    }
}
