package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.CreateRuleConditionDto;
import com.iviet.ivshs.dto.RuleConditionDto;
import com.iviet.ivshs.dto.UpdateRuleConditionDto;
import com.iviet.ivshs.entities.RuleCondition;

public interface RuleConditionMapper extends 
    BaseMapper<RuleCondition, RuleConditionDto>,
    CreateMapper<RuleCondition, CreateRuleConditionDto>,
    UpdateMapper<RuleCondition, UpdateRuleConditionDto> {
}
