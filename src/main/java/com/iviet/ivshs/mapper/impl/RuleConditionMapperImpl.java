package com.iviet.ivshs.mapper.impl;

import com.iviet.ivshs.dto.CreateRuleConditionDto;
import com.iviet.ivshs.dto.RuleConditionDto;
import com.iviet.ivshs.dto.UpdateRuleConditionDto;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.mapper.RuleConditionMapper;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RuleConditionMapperImpl implements RuleConditionMapper {

  @Override
  public RuleCondition toEntity(RuleConditionDto dto) {
    if (dto == null) {
      return null;
    }

    RuleCondition condition = new RuleCondition();
    condition.setId(dto.id());
    condition.setSortOrder(dto.sortOrder());
    condition.setDataSource(dto.dataSource());
    condition.setResourceParam(dto.resourceParam());
    condition.setOperator(dto.operator());
    condition.setValue(dto.value());
    condition.setNextLogic(dto.nextLogic());

    return condition;
  }

  @Override
  public RuleCondition fromCreateDto(CreateRuleConditionDto dto) {
    if (dto == null) {
      return null;
    }

    RuleCondition condition = new RuleCondition();
    condition.setSortOrder(dto.sortOrder());
    condition.setDataSource(dto.dataSource());
    condition.setResourceParam(dto.resourceParam());
    condition.setOperator(dto.operator());
    condition.setValue(dto.value());
    condition.setNextLogic(dto.nextLogic());

    return condition;
  }

  @Override
  public void updateFromDto(UpdateRuleConditionDto dto, RuleCondition condition) {
    if (dto == null || condition == null) {
      return;
    }

    condition.setSortOrder(dto.sortOrder());
    condition.setDataSource(dto.dataSource());
    condition.setResourceParam(dto.resourceParam());
    condition.setOperator(dto.operator());
    condition.setValue(dto.value());
    condition.setNextLogic(dto.nextLogic());
  }

  @Override
  public RuleConditionDto toDto(RuleCondition entity) {
    if (entity == null) {
      return null;
    }

    return RuleConditionDto.builder()
        .id(entity.getId())
        .sortOrder(entity.getSortOrder())
        .dataSource(entity.getDataSource())
        .resourceParam(entity.getResourceParam())
        .operator(entity.getOperator())
        .value(entity.getValue())
        .nextLogic(entity.getNextLogic())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  @Override
  public List<RuleConditionDto> toDtoList(List<RuleCondition> entities) {
    if (entities == null) {
      return Collections.emptyList();
    }

    return entities.stream()
        .map(this::toDto)
        .toList();
  }
}
