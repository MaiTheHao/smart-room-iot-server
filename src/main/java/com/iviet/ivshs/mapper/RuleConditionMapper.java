package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.rule.CreateRuleConditionDto;
import com.iviet.ivshs.dto.rule.RuleConditionDto;
import com.iviet.ivshs.dto.rule.UpdateRuleConditionDto;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.mapper.base.BaseMapper;

public interface RuleConditionMapper extends
    BaseMapper<RuleCondition, RuleConditionDto>,
    CreateMapper<RuleCondition, CreateRuleConditionDto>,
    UpdateMapper<RuleCondition, UpdateRuleConditionDto> {
}
