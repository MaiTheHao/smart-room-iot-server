package com.iviet.ivshs.mapper.impl;

import com.iviet.ivshs.dto.CreateRuleActionDto;
import com.iviet.ivshs.dto.RuleActionDto;
import com.iviet.ivshs.dto.UpdateRuleActionDto;
import com.iviet.ivshs.entities.RuleAction;
import com.iviet.ivshs.mapper.RuleActionMapper;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RuleActionMapperImpl implements RuleActionMapper {

    @Override
    public RuleAction toEntity(RuleActionDto dto) {
        if (dto == null) {
            return null;
        }

        RuleAction entity = new RuleAction();
        entity.setId(dto.id());
        entity.setExecutionOrder(dto.executionOrder());
        entity.setTargetDeviceId(dto.targetDeviceId());
        entity.setTargetDeviceCategory(dto.targetDeviceCategory());
        entity.setActionParams(dto.actionParams());

        return entity;
    }

    @Override
    public RuleAction fromCreateDto(CreateRuleActionDto dto) {
        if (dto == null) {
            return null;
        }

        RuleAction entity = new RuleAction();
        entity.setExecutionOrder(dto.executionOrder());
        entity.setTargetDeviceId(dto.targetDeviceId());
        entity.setTargetDeviceCategory(dto.targetDeviceCategory());
        entity.setActionParams(dto.actionParams());

        return entity;
    }

    @Override
    public void updateFromDto(UpdateRuleActionDto dto, RuleAction entity) {
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
    public RuleActionDto toDto(RuleAction entity) {
        if (entity == null) {
            return null;
        }

        return RuleActionDto.builder()
                .id(entity.getId())
                .executionOrder(entity.getExecutionOrder())
                .targetDeviceId(entity.getTargetDeviceId())
                .targetDeviceCategory(entity.getTargetDeviceCategory())
                .actionParams(entity.getActionParams())
                .build();
    }

    @Override
    public List<RuleActionDto> toDtoList(List<RuleAction> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toDto)
                .toList();
    }
}
