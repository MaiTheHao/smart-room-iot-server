package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.CreateRuleDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.RuleDto;
import com.iviet.ivshs.dto.UpdateRuleDto;

import java.util.List;

public interface RuleService {

    void executeGlobalRuleScan();

    void reloadAllRules();

    RuleDto create(CreateRuleDto request);

    RuleDto update(Long id, UpdateRuleDto request);

    void delete(Long id);

    RuleDto getById(Long id);

    List<RuleDto> getAll();

    PaginatedResponse<RuleDto> getList(int page, int size);

    void toggleIsActive(Long id, boolean isActive);
}
