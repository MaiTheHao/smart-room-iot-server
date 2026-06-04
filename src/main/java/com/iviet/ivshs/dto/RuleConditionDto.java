package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.shared.enumeration.ConditionLogic;
import com.iviet.ivshs.shared.enumeration.ConditionOperator;
import com.iviet.ivshs.shared.enumeration.RuleDataSource;
import lombok.Builder;

@Builder
public record RuleConditionDto(
                Long id,
                Integer sortOrder,
                RuleDataSource dataSource,
                JsonNode resourceParam,
                ConditionOperator operator,
                String value,
                ConditionLogic nextLogic) {
}
