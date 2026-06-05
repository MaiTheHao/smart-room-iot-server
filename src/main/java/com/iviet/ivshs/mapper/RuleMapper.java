package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.rule.CreateRuleDto;
import com.iviet.ivshs.dto.rule.RuleDto;
import com.iviet.ivshs.dto.rule.UpdateRuleDto;
import com.iviet.ivshs.entities.Rule;
import com.iviet.ivshs.mapper.base.BaseMapper;

public interface RuleMapper extends
                BaseMapper<Rule, RuleDto>,
                CreateMapper<Rule, CreateRuleDto>,
                UpdateMapper<Rule, UpdateRuleDto> {
}
