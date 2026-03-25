package com.iviet.ivshs.dto;

import java.util.List;
import jakarta.validation.Valid;
import lombok.Builder;

@Builder
public record UpdateRuleV2Dto(
        String name,
        Integer priority,
        Boolean isActive,
        String cronExpression,
        @Valid
        List<UpdateRuleConditionV2Dto> conditions,
        @Valid
        List<UpdateRuleActionV2Dto> actions
) {}
