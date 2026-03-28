package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.CreateRuleV2Dto;
import com.iviet.ivshs.dto.RuleV2Dto;
import com.iviet.ivshs.dto.UpdateRuleV2Dto;
import com.iviet.ivshs.entities.RuleV2;

public interface RuleV2Mapper extends 
  BaseMapper<RuleV2, RuleV2Dto>,
  CreateMapper<RuleV2, CreateRuleV2Dto>,
  UpdateMapper<RuleV2, UpdateRuleV2Dto>  
{}
