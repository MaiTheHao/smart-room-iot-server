package com.iviet.ivshs.mapper.impl;

import com.iviet.ivshs.dto.rule.CreateRuleConditionDto;
import com.iviet.ivshs.dto.rule.RuleConditionDto;
import com.iviet.ivshs.dto.rule.UpdateRuleConditionDto;
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

        RuleCondition entity = new RuleCondition();
        entity.setId(dto.id());
        entity.setSortOrder(dto.sortOrder());
        entity.setDataSource(dto.dataSource());
        entity.setResourceParam(dto.resourceParam());
        entity.setOperator(dto.operator());
        entity.setValue(dto.value());
        entity.setNextLogic(dto.nextLogic());

        return entity;
    }

    @Override
    public RuleCondition fromCreateDto(CreateRuleConditionDto dto) {
        if (dto == null) {
            return null;
        }

        RuleCondition entity = new RuleCondition();
        entity.setSortOrder(dto.sortOrder());
        entity.setDataSource(dto.dataSource());
        entity.setResourceParam(dto.resourceParam());
        entity.setOperator(dto.operator());
        entity.setValue(dto.value());
        entity.setNextLogic(dto.nextLogic());

        return entity;
    }

    @Override
    public void updateFromDto(UpdateRuleConditionDto dto, RuleCondition entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.sortOrder() != null) {
            entity.setSortOrder(dto.sortOrder());
        }
        if (dto.dataSource() != null) {
            entity.setDataSource(dto.dataSource());
        }
        if (dto.resourceParam() != null) {
            entity.setResourceParam(dto.resourceParam());
        }
        if (dto.operator() != null) {
            entity.setOperator(dto.operator());
        }
        if (dto.value() != null) {
            entity.setValue(dto.value());
        }
        if (dto.nextLogic() != null) {
            entity.setNextLogic(dto.nextLogic());
        }
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
