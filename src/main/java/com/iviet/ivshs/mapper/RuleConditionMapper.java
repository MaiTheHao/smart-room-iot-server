package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.CreateRuleConditionDto;
import com.iviet.ivshs.dto.RuleConditionDto;
import com.iviet.ivshs.dto.UpdateRuleConditionDto;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.mapper.base.BaseMapper;

public interface RuleConditionMapper extends
    BaseMapper<RuleCondition, RuleConditionDto>,
    CreateMapper<RuleCondition, CreateRuleConditionDto>,
    UpdateMapper<RuleCondition, UpdateRuleConditionDto> {
}
