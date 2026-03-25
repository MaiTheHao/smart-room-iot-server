package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.enumeration.ConditionLogic;
import com.iviet.ivshs.enumeration.ConditionOperator;
import com.iviet.ivshs.enumeration.RuleDataSource;
import lombok.Builder;

@Builder
public record RuleConditionV2Dto(
        Long id,
        Integer sortOrder,
        RuleDataSource dataSource,
        JsonNode resourceParam,
        ConditionOperator operator,
        String valueParam,
        ConditionLogic nextLogic
) {}
