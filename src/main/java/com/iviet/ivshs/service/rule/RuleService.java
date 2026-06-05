package com.iviet.ivshs.service.rule;

import java.util.List;

import com.iviet.ivshs.dto.rule.CreateRuleDto;
import com.iviet.ivshs.dto.system.PaginatedResponse;
import com.iviet.ivshs.dto.rule.RuleDto;
import com.iviet.ivshs.dto.rule.UpdateRuleDto;
import com.iviet.ivshs.entities.Rule;

public interface RuleService {
  
  RuleDto create(CreateRuleDto dto);

  RuleDto update(Long ruleId, UpdateRuleDto dto);

  void delete(Long ruleId);

  RuleDto getById(Long ruleId);

  PaginatedResponse<RuleDto> getAll(int page, int size);

  List<RuleDto> getAllActive();

	// SYSTEM / JOB CONTROL  
	void toggleIsActive(Long ruleId, boolean isActive);

	void scheduleJob(Rule rule);

	void rescheduleJob(Rule rule);

	void unscheduleJob(Long ruleId);

	void executeRuleLogic(Long ruleId);

	void executeRuleImmediately(Long ruleId);

	void reloadAllRules();
}
