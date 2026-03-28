package com.iviet.ivshs.mapper.impl;

import com.iviet.ivshs.dto.CreateRuleActionV2Dto;
import com.iviet.ivshs.dto.RuleActionV2Dto;
import com.iviet.ivshs.dto.UpdateRuleActionV2Dto;
import com.iviet.ivshs.entities.RuleActionV2;
import com.iviet.ivshs.mapper.RuleActionV2Mapper;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RuleActionV2MapperImpl implements RuleActionV2Mapper {

    @Override
    public RuleActionV2 toEntity(RuleActionV2Dto dto) {
        if (dto == null) {
            return null;
        }

        RuleActionV2 entity = new RuleActionV2();
        entity.setId(dto.id());
        entity.setExecutionOrder(dto.executionOrder());
        entity.setTargetDeviceId(dto.targetDeviceId());
        entity.setTargetDeviceCategory(dto.targetDeviceCategory());
        entity.setActionParams(dto.actionParams());

        return entity;
    }

    @Override
    public RuleActionV2 fromCreateDto(CreateRuleActionV2Dto dto) {
        if (dto == null) {
            return null;
        }

        RuleActionV2 entity = new RuleActionV2();
        entity.setExecutionOrder(dto.executionOrder());
        entity.setTargetDeviceId(dto.targetDeviceId());
        entity.setTargetDeviceCategory(dto.targetDeviceCategory());
        entity.setActionParams(dto.actionParams());

        return entity;
    }

    @Override
    public void updateFromDto(UpdateRuleActionV2Dto dto, RuleActionV2 entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.executionOrder() != null) {
            entity.setExecutionOrder(dto.executionOrder());
        }
        if (dto.targetDeviceId() != null) {
            entity.setTargetDeviceId(dto.targetDeviceId());
        }
        if (dto.targetDeviceCategory() != null) {
            entity.setTargetDeviceCategory(dto.targetDeviceCategory());
        }
        if (dto.actionParams() != null) {
            entity.setActionParams(dto.actionParams());
        }
    }

    @Override
    public RuleActionV2Dto toDto(RuleActionV2 entity) {
        if (entity == null) {
            return null;
        }

        return RuleActionV2Dto.builder()
                .id(entity.getId())
                .executionOrder(entity.getExecutionOrder())
                .targetDeviceId(entity.getTargetDeviceId())
                .targetDeviceCategory(entity.getTargetDeviceCategory())
                .actionParams(entity.getActionParams())
                .build();
    }

    @Override
    public List<RuleActionV2Dto> toDtoList(List<RuleActionV2> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toDto)
                .toList();
    }
}
