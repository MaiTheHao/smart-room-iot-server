package com.iviet.ivshs.service.rule;

import java.util.List;

import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.CreateRuleDto;
import com.iviet.ivshs.dto.RuleDto;
import com.iviet.ivshs.dto.UpdateRuleDto;
import com.iviet.ivshs.entities.Rule;
import com.iviet.ivshs.service.base.SchedulableJobService;

public interface RuleService extends SchedulableJobService<Rule> {

	RuleDto create(CreateRuleDto dto);

	RuleDto update(Long ruleId, UpdateRuleDto dto);

	void delete(Long ruleId);

	RuleDto getById(Long ruleId);

	PaginatedResponse<RuleDto> getAll(int page, int size);

	List<RuleDto> getAllActive();

	void toggleIsActive(Long ruleId, boolean isActive);
}
