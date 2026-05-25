package com.iviet.ivshs.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dao.RuleDao;
import com.iviet.ivshs.dto.CreateRuleDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.RuleDto;
import com.iviet.ivshs.dto.UpdateRuleDto;
import com.iviet.ivshs.entities.Rule;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.RuleMapper;
import com.iviet.ivshs.schedule.rule.RuleProcessor;
import com.iviet.ivshs.service.RuleService;
import com.iviet.ivshs.service.strategy.DeviceControlServiceStrategy;
import com.iviet.ivshs.util.ScheduleUtil;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RuleServiceImpl implements RuleService {

  private final RuleDao ruleDao;
  private final RuleMapper ruleMapper;
  private final ScheduleUtil scheduleUtil;
  private final RuleProcessor ruleProcessor;
  private final ObjectMapper objectMapper;
  private final Validator validator;
  private final List<DeviceControlServiceStrategy<?>> controlStrategies;

  private java.util.Map<DeviceCategory, DeviceControlServiceStrategy<?>> strategyMap;

  @PostConstruct
  private void initStrategyMap() {
    strategyMap = controlStrategies.stream()
        .collect(Collectors.toUnmodifiableMap(
            DeviceControlServiceStrategy::getSupportedCategory,
            java.util.function.Function.identity()
        ));
  }

  @Override
  @Transactional
  public RuleDto create(CreateRuleDto dto) {
    log.info("Creating rule: name={}", dto.name());

    if (ruleDao.existsByName(dto.name())) {
      throw new BadRequestException("Rule name already exists: " + dto.name());
    }

    dto.actions().forEach(action -> validateActionParams(action.targetDeviceCategory(), action.actionParams()));

    Rule rule = ruleMapper.fromCreateDto(dto);
    ruleDao.save(rule);

    scheduleUtil.sync(rule);

    log.info("Rule created successfully: id={}, name={}", rule.getId(), rule.getName());
    return ruleMapper.toDto(rule);
  }

  @Override
  @Transactional
  public RuleDto update(Long ruleId, UpdateRuleDto dto) {
    log.info("Updating rule: id={}", ruleId);

    Rule rule = ruleDao.findById(ruleId)
        .orElseThrow(() -> new NotFoundException("Rule not found: " + ruleId));

    if (dto.name() != null && !rule.getName().equals(dto.name()) && ruleDao.existsByNameAndIdNot(dto.name(), ruleId)) {
      throw new BadRequestException("Rule name already exists: " + dto.name());
    }

    if (dto.actions() != null) {
        dto.actions().forEach(action -> {
            DeviceCategory category = action.targetDeviceCategory();
            JsonNode params = action.actionParams();
            if (category != null && params != null) {
                validateActionParams(category, params);
            }
        });
    }

    ruleMapper.updateFromDto(dto, rule);
    ruleDao.update(rule);
    
    scheduleUtil.sync(rule);

    log.info("Rule updated successfully: id={}, name={}", ruleId, rule.getName());
    return ruleMapper.toDto(rule);
  }

  @Override
  @Transactional
  public void delete(Long ruleId) {
    log.info("Deleting rule: id={}", ruleId);
    Rule rule = ruleDao.findById(ruleId)
        .orElseThrow(() -> new NotFoundException("Rule not found: " + ruleId));
    
    scheduleUtil.delete(rule);
    ruleDao.deleteById(ruleId);
  }

  @Override
  public RuleDto getById(Long ruleId) {
    return ruleDao.findByIdWithConditionsAndActions(ruleId)
        .map(ruleMapper::toDto)
        .orElseThrow(() -> new NotFoundException("Rule not found: " + ruleId));
  }

  @Override
  public PaginatedResponse<RuleDto> getAll(int page, int size) {
    List<Rule> rules = ruleDao.findAllPaginated(page, size);
    List<RuleDto> dtos = ruleMapper.toDtoList(rules);
    long total = ruleDao.count();
    return new PaginatedResponse<>(dtos, page, size, total);
  }

  @Override
  public List<RuleDto> getAllActive() {
    return ruleMapper.toDtoList(ruleDao.findAllActive());
  }

  @Override
  @Transactional
  public void toggleIsActive(Long ruleId, boolean isActive) {
    log.info("Toggling rule status: id={}, isActive={}", ruleId, isActive);
    Rule rule = ruleDao.findById(ruleId)
        .orElseThrow(() -> new NotFoundException("Rule not found: " + ruleId));

    if (Objects.equals(rule.getIsActive(), isActive)) return;

    rule.setIsActive(isActive);
    ruleDao.update(rule);
    scheduleUtil.sync(rule);
  }

  @Override
  public void scheduleJob(Rule rule) {
    scheduleUtil.sync(rule);
  }

  @Override
  public void rescheduleJob(Rule rule) {
    scheduleUtil.sync(rule);
  }

  @Override
  public void unscheduleJob(Long ruleId) {
    Rule rule = ruleDao.findById(ruleId)
        .orElseThrow(() -> new NotFoundException("Rule not found: " + ruleId));
    scheduleUtil.delete(rule);
  }

  @Override
  public void executeRuleLogic(Long ruleId) {
    Rule rule = ruleDao.findByIdWithConditionsAndActions(ruleId).orElseThrow(() -> new NotFoundException("Rule not found: " + ruleId));

    if (Boolean.FALSE.equals(rule.getIsActive())) return;
    ruleProcessor.process(rule);
  }

  @Override
  public void executeRuleImmediately(Long ruleId) {
    Rule rule = ruleDao.findById(ruleId).orElseThrow(() -> new NotFoundException("Rule not found: " + ruleId));
    scheduleUtil.triggerNow(rule);
  }

  @Override
  @Transactional
  public void reloadAllRules() {
    scheduleUtil.deleteJobGroup(Rule.JOB_GROUP);
    List<Rule> activeRules = ruleDao.findAllActiveWithConditionsAndActions();
    for (Rule rule : activeRules) {
      try {
        scheduleUtil.sync(rule);
      } catch (Exception e) {
        log.error("Failed to reload rule: id={}", rule.getId(), e);
      }
    }
  }

  private void validateActionParams(DeviceCategory category, JsonNode params) {
    DeviceControlServiceStrategy<?> strategy = java.util.Optional.ofNullable(strategyMap.get(category))
        .orElseThrow(() -> new BadRequestException("Unsupported category: " + category));

    try {
      Object dto = objectMapper.treeToValue(params, strategy.getControlDtoClass());
      var violations = validator.validate(dto);
      if (!violations.isEmpty()) {
        String errorMsg = violations.stream()
            .map(v -> v.getPropertyPath() + " " + v.getMessage())
            .collect(Collectors.joining(", "));
        throw new BadRequestException(errorMsg);
      }
    } catch (JsonProcessingException e) {
      throw new BadRequestException("Invalid JSON format for action parameters");
    }
  }
}
