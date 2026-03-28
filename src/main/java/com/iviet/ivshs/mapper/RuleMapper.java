package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.CreateRuleDto;
import com.iviet.ivshs.dto.RuleDto;
import com.iviet.ivshs.dto.UpdateRuleDto;
import com.iviet.ivshs.entities.Rule;

public interface RuleMapper extends 
    BaseMapper<Rule, RuleDto>,
    CreateMapper<Rule, CreateRuleDto>,
    UpdateMapper<Rule, UpdateRuleDto> {
}