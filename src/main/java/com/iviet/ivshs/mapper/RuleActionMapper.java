package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.CreateRuleActionDto;
import com.iviet.ivshs.dto.RuleActionDto;
import com.iviet.ivshs.dto.UpdateRuleActionDto;
import com.iviet.ivshs.entities.RuleAction;

public interface RuleActionMapper extends
  BaseMapper<RuleAction, RuleActionDto>,
  CreateMapper<RuleAction, CreateRuleActionDto>,
  UpdateMapper<RuleAction, UpdateRuleActionDto>
{}
