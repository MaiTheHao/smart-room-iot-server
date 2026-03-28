package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.CreateRuleV2Dto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.RuleV2Dto;
import com.iviet.ivshs.dto.UpdateRuleV2Dto;
import com.iviet.ivshs.entities.RuleV2;

public interface RuleV2Service {
  
  RuleV2Dto create(CreateRuleV2Dto dto);

  RuleV2Dto update(Long ruleId, UpdateRuleV2Dto dto);

  void delete(Long ruleId);

  RuleV2Dto getById(Long ruleId);

  PaginatedResponse<RuleV2Dto> getAll(int page, int size);

  List<RuleV2Dto> getAllActive();

	// SYSTEM / JOB CONTROL  
	void toggleIsActive(Long ruleId, boolean isActive);

	void scheduleJob(RuleV2 rule);

	void rescheduleJob(RuleV2 rule);

	void unscheduleJob(Long ruleId);

	void executeRuleLogic(Long ruleId);

	void executeRuleImmediately(Long ruleId);

	void reloadAllRules();
}
