package com.iviet.ivshs.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dao.RuleV2Dao;
import com.iviet.ivshs.dto.CreateRuleV2Dto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.RuleV2Dto;
import com.iviet.ivshs.dto.UpdateRuleV2Dto;
import com.iviet.ivshs.entities.RuleV2;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.RuleV2Mapper;
import com.iviet.ivshs.schedule.rule.RuleV2Processor;
import com.iviet.ivshs.service.RuleV2Service;
import com.iviet.ivshs.service.strategy.DeviceControlServiceStrategy;
import com.iviet.ivshs.util.ScheduleUtil;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "RULEV2-SERVICE")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RuleV2ServiceImpl implements RuleV2Service {

  private final RuleV2Dao ruleV2Dao;
  private final RuleV2Mapper ruleV2Mapper;
  private final ScheduleUtil scheduleUtil;
  private final RuleV2Processor ruleV2Processor;
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
  public RuleV2Dto create(CreateRuleV2Dto dto) {
    log.info("Creating RuleV2: {}", dto.name());

    if (ruleV2Dao.existsByName(dto.name())) {
      throw new BadRequestException("Rule name already exists: " + dto.name());
    }

    dto.actions().forEach(action -> validateActionParams(action.targetDeviceCategory(), action.actionParams()));

    RuleV2 rule = ruleV2Mapper.fromCreateDto(dto);
    ruleV2Dao.save(rule);

    scheduleUtil.sync(rule);

    log.info("Created RuleV2 ID: {}", rule.getId());
    return ruleV2Mapper.toDto(rule);
  }

  @Override
  @Transactional
  public RuleV2Dto update(Long ruleId, UpdateRuleV2Dto dto) {
    log.info("Updating RuleV2 ID: {}", ruleId);

    RuleV2 rule = ruleV2Dao.findById(ruleId)
        .orElseThrow(() -> new NotFoundException("RuleV2 not found: " + ruleId));

    if (dto.name() != null && !rule.getName().equals(dto.name()) && ruleV2Dao.existsByNameAndIdNot(dto.name(), ruleId)) {
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

    ruleV2Mapper.updateFromDto(dto, rule);
    ruleV2Dao.update(rule);
    
    scheduleUtil.sync(rule);

    log.info("Updated RuleV2 ID: {}", ruleId);
    return ruleV2Mapper.toDto(rule);
  }

  @Override
  @Transactional
  public void delete(Long ruleId) {
    log.info("Deleting RuleV2 ID: {}", ruleId);
    RuleV2 rule = ruleV2Dao.findById(ruleId)
        .orElseThrow(() -> new NotFoundException("RuleV2 not found: " + ruleId));
    
    scheduleUtil.delete(rule);
    ruleV2Dao.deleteById(ruleId);
  }

  @Override
  public RuleV2Dto getById(Long ruleId) {
    return ruleV2Dao.findById(ruleId)
        .map(ruleV2Mapper::toDto)
        .orElseThrow(() -> new NotFoundException("RuleV2 not found: " + ruleId));
  }

  @Override
  public PaginatedResponse<RuleV2Dto> getAll(int page, int size) {
    List<RuleV2> rules = ruleV2Dao.findAllPaginated(page, size);
    List<RuleV2Dto> dtos = ruleV2Mapper.toDtoList(rules);
    long total = ruleV2Dao.count();
    return new PaginatedResponse<>(dtos, page, size, total);
  }

  @Override
  public List<RuleV2Dto> getAllActive() {
    return ruleV2Mapper.toDtoList(ruleV2Dao.findAllActive());
  }

  @Override
  @Transactional
  public void toggleIsActive(Long ruleId, boolean isActive) {
    log.info("Toggle RuleV2 ID: {} to {}", ruleId, isActive);
    RuleV2 rule = ruleV2Dao.findById(ruleId)
        .orElseThrow(() -> new NotFoundException("RuleV2 not found: " + ruleId));

    if (Objects.equals(rule.getIsActive(), isActive)) return;

    rule.setIsActive(isActive);
    ruleV2Dao.update(rule);
    scheduleUtil.sync(rule);
  }

  @Override
  public void scheduleJob(RuleV2 rule) {
    scheduleUtil.sync(rule);
  }

  @Override
  public void rescheduleJob(RuleV2 rule) {
    scheduleUtil.sync(rule);
  }

  @Override
  public void unscheduleJob(Long ruleId) {
    RuleV2 rule = ruleV2Dao.findById(ruleId)
        .orElseThrow(() -> new NotFoundException("RuleV2 not found: " + ruleId));
    scheduleUtil.delete(rule);
  }

  @Override
  @Transactional
  public void executeRuleLogic(Long ruleId) {
    RuleV2 rule = ruleV2Dao.findByIdWithConditionsAndActions(ruleId)
        .orElseThrow(() -> new NotFoundException("RuleV2 not found: " + ruleId));

    if (Boolean.FALSE.equals(rule.getIsActive())) return;
    ruleV2Processor.process(rule);
  }

  @Override
  @Transactional
  public void executeRuleImmediately(Long ruleId) {
    RuleV2 rule = ruleV2Dao.findById(ruleId)
        .orElseThrow(() -> new NotFoundException("RuleV2 not found: " + ruleId));
    scheduleUtil.triggerNow(rule);
  }

  @Override
  @Transactional
  public void reloadAllRules() {
    scheduleUtil.deleteJobGroup(RuleV2.JOB_GROUP);
    List<RuleV2> activeRules = ruleV2Dao.findAllActiveWithConditionsAndActions();
    for (RuleV2 rule : activeRules) {
      try {
        scheduleUtil.sync(rule);
      } catch (Exception e) {
        log.error("Failed to reload RuleV2 ID {}: {}", rule.getId(), e.getMessage());
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
