package com.iviet.ivshs.mapper.impl;

import com.iviet.ivshs.dto.CreateRuleConditionV2Dto;
import com.iviet.ivshs.dto.RuleConditionV2Dto;
import com.iviet.ivshs.dto.UpdateRuleConditionV2Dto;
import com.iviet.ivshs.entities.RuleConditionV2;
import com.iviet.ivshs.mapper.RuleConditionV2Mapper;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RuleConditionV2MapperImpl implements RuleConditionV2Mapper {

    @Override
    public RuleConditionV2 toEntity(RuleConditionV2Dto dto) {
        if (dto == null) {
            return null;
        }

        RuleConditionV2 entity = new RuleConditionV2();
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
    public RuleConditionV2 fromCreateDto(CreateRuleConditionV2Dto dto) {
        if (dto == null) {
            return null;
        }

        RuleConditionV2 entity = new RuleConditionV2();
        entity.setSortOrder(dto.sortOrder());
        entity.setDataSource(dto.dataSource());
        entity.setResourceParam(dto.resourceParam());
        entity.setOperator(dto.operator());
        entity.setValue(dto.value());
        entity.setNextLogic(dto.nextLogic());

        return entity;
    }

    @Override
    public void updateFromDto(UpdateRuleConditionV2Dto dto, RuleConditionV2 entity) {
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
    public RuleConditionV2Dto toDto(RuleConditionV2 entity) {
        if (entity == null) {
            return null;
        }

        return RuleConditionV2Dto.builder()
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
    public List<RuleConditionV2Dto> toDtoList(List<RuleConditionV2> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toDto)
                .toList();
    }
}
