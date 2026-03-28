package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.CreateRuleActionV2Dto;
import com.iviet.ivshs.dto.RuleActionV2Dto;
import com.iviet.ivshs.dto.UpdateRuleActionV2Dto;
import com.iviet.ivshs.entities.RuleActionV2;

public interface RuleActionV2Mapper extends
  BaseMapper<RuleActionV2, RuleActionV2Dto>,
  CreateMapper<RuleActionV2, CreateRuleActionV2Dto>,
  UpdateMapper<RuleActionV2, UpdateRuleActionV2Dto>
{}
