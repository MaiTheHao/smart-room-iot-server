package com.iviet.ivshs.service.rule.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.dao.RuleDao;
import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.dto.rule.CreateRuleDto;
import com.iviet.ivshs.dto.rule.RuleDto;
import com.iviet.ivshs.dto.rule.UpdateRuleDto;
import com.iviet.ivshs.entities.Rule;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.mapper.RuleMapper;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.DeviceSpecificType;
import com.iviet.ivshs.shared.util.DeviceCapabilityRegistry;
import com.iviet.ivshs.shared.exception.BadRequestException;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.service.control.DeviceControlServiceStrategy;
import com.iviet.ivshs.service.base.AbstractSchedulableJobService;
import com.iviet.ivshs.service.rule.RuleService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RuleServiceImpl extends AbstractSchedulableJobService<Rule> implements RuleService {

  private final RuleDao ruleDao;
  private final RuleMapper ruleMapper;
  private final ObjectMapper objectMapper;
  private final Validator validator;
  private final List<DeviceControlServiceStrategy<?>> controlStrategies;
  private final FanDao fanDao;
  private final LightDao lightDao;
  private final AirConditionDao airConditionDao;
  private final com.iviet.ivshs.service.alert.AlertService alertService;

  private Map<DeviceCategory, DeviceControlServiceStrategy<?>> strategyMap;

  @PostConstruct
  private void initStrategyMap() {
    strategyMap = controlStrategies.stream()
        .collect(Collectors.toUnmodifiableMap(DeviceControlServiceStrategy::getSupportedCategory, Function.identity()));
  }

  @Override
  @Transactional
  public RuleDto create(CreateRuleDto dto) {
    log.info("Creating rule: name={}", dto.name());

    if (ruleDao.existsByName(dto.name())) {
      throw new BadRequestException("Rule name already exists: " + dto.name());
    }

    dto.actions()
        .forEach(action -> validateActionParams(action.targetDeviceCategory(), action.targetDeviceId(), action.actionParams()));

    Rule rule = ruleMapper.fromCreateDto(dto);
    ruleDao.save(rule);

    jobScheduleService.sync(rule);

    log.info("Rule created successfully: id={}, name={}", rule.getId(), rule.getName());
    return ruleMapper.toDto(rule);
  }

  @Override
  @Transactional
  public RuleDto update(Long ruleId, UpdateRuleDto dto) {
    log.info("Updating rule: id={}", ruleId);

    Rule rule = ruleDao.findById(ruleId)
        .orElseThrow(() -> new NotFoundException("Rule not found: " + ruleId));

    if (dto.name() != null && !rule.getName()
        .equals(dto.name()) && ruleDao.existsByNameAndIdNot(dto.name(), ruleId)) {
      throw new BadRequestException("Rule name already exists: " + dto.name());
    }

    if (dto.actions() != null) {
      dto.actions()
          .stream()
          .filter(action -> action.targetDeviceCategory() != null && action.targetDeviceId() != null && action.actionParams() != null)
          .forEach(action -> validateActionParams(action.targetDeviceCategory(), action.targetDeviceId(), action.actionParams()));
    }

    ruleMapper.updateFromDto(dto, rule);
    ruleDao.update(rule);

    jobScheduleService.sync(rule);

    log.info("Rule updated successfully: id={}, name={}", ruleId, rule.getName());
    return ruleMapper.toDto(rule);
  }

  @Override
  @Transactional
  public void delete(Long ruleId) {
    log.info("Deleting rule: id={}", ruleId);
    Rule rule = ruleDao.findById(ruleId)
        .orElseThrow(() -> new NotFoundException("Rule not found: " + ruleId));

    jobScheduleService.delete(rule);
    alertService.deleteAlertsByRuleId(ruleId);
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

    if (Objects.equals(rule.getIsActive(), isActive)) {
      return;
    }

    rule.setIsActive(isActive);
    ruleDao.update(rule);
    jobScheduleService.sync(rule);
  }

  @Override
  protected Rule getEntityById(Long id) {
    return ruleDao.findById(id)
        .orElseThrow(() -> new NotFoundException("Rule not found: " + id));
  }

  @Override
  protected List<Rule> getAllActiveEntities() {
    return ruleDao.findAllActiveWithConditionsAndActions();
  }

  @Override
  protected String getJobGroup() {
    return Rule.JOB_GROUP;
  }

  // --- PRIVATE HELPER METHODS ---

  private void validateActionParams(DeviceCategory category, Long targetDeviceId, JsonNode params) {
    DeviceControlServiceStrategy<?> strategy = Optional.ofNullable(strategyMap.get(category))
        .orElseThrow(() -> new BadRequestException("Unsupported category: " + category));

    DeviceSpecificType specificType = getSpecificType(category, targetDeviceId);

    validateCapabilities(category, specificType, params);
    validateDtoConstraints(params, strategy.getControlDtoClass());
  }

  private DeviceSpecificType getSpecificType(DeviceCategory category, Long targetDeviceId) {
    if (category == null) {
      return DeviceSpecificType.GPIO;
    }

    return switch (category) {
      case FAN -> fanDao.findById(targetDeviceId)
          .map(Fan::getSpecificType)
          .orElseThrow(() -> new NotFoundException("Fan not found with id: " + targetDeviceId));
      case LIGHT -> lightDao.findById(targetDeviceId)
          .map(Light::getSpecificType)
          .orElseThrow(() -> new NotFoundException("Light not found with id: " + targetDeviceId));
      case AIR_CONDITION -> airConditionDao.findById(targetDeviceId)
          .map(AirCondition::getSpecificType)
          .orElseThrow(() -> new NotFoundException("AirCondition not found with id: " + targetDeviceId));
      default -> DeviceSpecificType.GPIO;
    };
  }

  private void validateCapabilities(DeviceCategory category, DeviceSpecificType specificType, JsonNode params) {
    if (params == null || !params.isObject()) {
      return;
    }

    Iterator<String> fieldNames = params.fieldNames();
    while (fieldNames.hasNext()) {
      String field = fieldNames.next();
      JsonNode valNode = params.get(field);
      if (valNode != null && !valNode.isNull()) {
        if (!DeviceCapabilityRegistry.isSupported(category, specificType, field)) {
          throw new BadRequestException("Device category " + category + " with type " + specificType + " does not support parameter: " + field);
        }
      }
    }
  }

  private void validateDtoConstraints(JsonNode params, Class<?> dtoClass) {
    try {
      Object dto = objectMapper.treeToValue(params, dtoClass);
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
