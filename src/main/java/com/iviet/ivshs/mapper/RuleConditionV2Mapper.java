package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.CreateRuleConditionV2Dto;
import com.iviet.ivshs.dto.RuleConditionV2Dto;
import com.iviet.ivshs.dto.UpdateRuleConditionV2Dto;
import com.iviet.ivshs.entities.RuleConditionV2;

public interface RuleConditionV2Mapper extends
  BaseMapper<RuleConditionV2, RuleConditionV2Dto>,
  CreateMapper<RuleConditionV2, CreateRuleConditionV2Dto>,
  UpdateMapper<RuleConditionV2, UpdateRuleConditionV2Dto>
{}
