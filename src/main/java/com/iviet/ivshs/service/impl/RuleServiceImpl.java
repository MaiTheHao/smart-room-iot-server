package com.iviet.ivshs.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.RuleDao;
import com.iviet.ivshs.dto.CreateRuleDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.RuleDto;
import com.iviet.ivshs.dto.UpdateRuleDto;
import com.iviet.ivshs.entities.Rule;
import com.iviet.ivshs.service.AbstractSchedulableJobService;
import com.iviet.ivshs.service.ActionService;
import com.iviet.ivshs.service.AlertConfigService;
import com.iviet.ivshs.service.ConditionService;
import com.iviet.ivshs.service.RuleService;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.enumeration.ConditionOwnerCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import com.iviet.ivshs.shared.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RuleServiceImpl extends AbstractSchedulableJobService<Rule> implements RuleService {

  private final RuleDao ruleDao;
  private final ConditionService conditionService;
  private final ActionService actionService;
  private final AlertConfigService alertConfigService;

  @Override
  @Transactional
  public RuleDto create(CreateRuleDto dto) {
    if (ruleDao.existsByName(dto.name())) {
      throw new BadRequestException("Rule name already exists: " + dto.name());
    }

    Rule rule = dto.toEntity();
    ruleDao.save(rule);

    jobScheduleService.sync(rule);

    return RuleDto.fromEntity(rule);
  }

  @Override
  @Transactional
  public RuleDto update(Long ruleId, UpdateRuleDto dto) {
    Rule rule = ruleDao
        .findById(ruleId)
        .orElseThrow(() -> new NotFoundException("Rule not found: " + ruleId));

    if (dto.name() != null && !dto.name().equals(rule.getName())) {
      if (ruleDao.existsByNameAndIdNot(dto.name(), ruleId)) {
        throw new BadRequestException("Rule name already exists: " + dto.name());
      }
    }

    dto.updateEntity(rule);
    ruleDao.update(rule);

    jobScheduleService.sync(rule);

    return RuleDto.fromEntity(rule);
  }

  @Override
  @Transactional
  public void delete(Long ruleId) {
    Rule rule = ruleDao
        .findById(ruleId)
        .orElseThrow(() -> new NotFoundException("Rule not found: " + ruleId));

    jobScheduleService.delete(rule);

    try {
      alertConfigService.deleteAllBySource(AlertNamespace.RULE, String.valueOf(ruleId));
    } catch (Exception ignored) {
    }

    conditionService.deleteByOwner(ConditionOwnerCategory.RULE, ruleId);
    actionService.deleteByOwner(ActionOwnerCategory.RULE, ruleId);

    ruleDao.delete(rule);
  }

  @Override
  public RuleDto getById(Long ruleId) {
    return ruleDao
        .findById(ruleId)
        .map(RuleDto::fromEntity)
        .orElseThrow(() -> new NotFoundException("Rule not found: " + ruleId));
  }

  @Override
  public PaginatedResponse<RuleDto> getAll(int page, int size) {
    List<Rule> rules = ruleDao.findAllPaginated(page, size);
    List<RuleDto> dtos = rules.stream().map(RuleDto::fromEntity).toList();
    long total = ruleDao.count();
    return new PaginatedResponse<>(dtos, page, size, total);
  }

  @Override
  public List<RuleDto> getAllActive() {
    return ruleDao.findAllActive().stream().map(RuleDto::fromEntity).toList();
  }

  @Override
  @Transactional
  public void updateActiveStatus(Long ruleId, boolean active) {
    Rule rule = ruleDao
        .findById(ruleId)
        .orElseThrow(() -> new NotFoundException("Rule not found: " + ruleId));

    if (Objects.equals(rule.getIsActive(), active)) {
      return;
    }

    rule.setIsActive(active);
    ruleDao.update(rule);
    jobScheduleService.sync(rule);
  }

  @Override
  protected Rule getEntityById(Long id) {
    return ruleDao.findById(id).orElseThrow(() -> new NotFoundException("Rule not found: " + id));
  }

  @Override
  protected List<Rule> getAllActiveEntities() {
    return ruleDao.findAllActive();
  }

  @Override
  protected String getJobGroup() {
    return Rule.JOB_GROUP;
  }
}
