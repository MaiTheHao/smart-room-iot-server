package com.iviet.ivshs.mapper.impl;

import com.iviet.ivshs.dto.CreateRuleV2Dto;
import com.iviet.ivshs.dto.RuleV2Dto;
import com.iviet.ivshs.dto.UpdateRuleV2Dto;
import com.iviet.ivshs.entities.RuleActionV2;
import com.iviet.ivshs.entities.RuleConditionV2;
import com.iviet.ivshs.entities.RuleV2;
import com.iviet.ivshs.mapper.RuleActionV2Mapper;
import com.iviet.ivshs.mapper.RuleConditionV2Mapper;
import com.iviet.ivshs.mapper.RuleV2Mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RuleV2MapperImpl implements RuleV2Mapper {

    private final RuleConditionV2Mapper ruleConditionMapper;
    private final RuleActionV2Mapper ruleActionMapper;

    @Override
    public RuleV2 toEntity(RuleV2Dto dto) {
        if (dto == null) {
            return null;
        }

        RuleV2 rule = new RuleV2();
        rule.setId(dto.id());
        rule.setName(dto.name());
        rule.setPriority(dto.priority());
        rule.setIsActive(dto.isActive());
        rule.setRoomId(dto.roomId());
        rule.setIntervalSeconds(dto.intervalSeconds());
        rule.setIsInterval(dto.intervalSeconds() != null && dto.intervalSeconds() > 0);

        if (dto.conditions() != null && !dto.conditions().isEmpty()) {
            dto.conditions().forEach(condDto -> {
                RuleConditionV2 condition = ruleConditionMapper.toEntity(condDto);
                rule.addCondition(condition);
            });
        }

        if (dto.actions() != null && !dto.actions().isEmpty()) {
            dto.actions().forEach(actDto -> {
                RuleActionV2 action = ruleActionMapper.toEntity(actDto);
                rule.addAction(action);
            });
        }

        return rule;
    }

    @Override
    public RuleV2 fromCreateDto(CreateRuleV2Dto dto) {
        if (dto == null) {
            return null;
        }

        RuleV2 rule = new RuleV2();
        rule.setName(dto.name());
        rule.setPriority(dto.priority());
        rule.setRoomId(dto.roomId());
        rule.setIntervalSeconds(dto.intervalSeconds());
        rule.setIsInterval(dto.intervalSeconds() != null && dto.intervalSeconds() > 0);
        rule.setIsActive(true);

        if (dto.conditions() != null) {
            dto.conditions().forEach(condDto -> {
                RuleConditionV2 condition = ruleConditionMapper.fromCreateDto(condDto);
                rule.addCondition(condition);
            });
        }

        if (dto.actions() != null) {
            dto.actions().forEach(actDto -> {
                RuleActionV2 action = ruleActionMapper.fromCreateDto(actDto);
                rule.addAction(action);
            });
        }

        return rule;
    }

    @Override
    public void updateFromDto(UpdateRuleV2Dto dto, RuleV2 rule) {
        if (dto == null || rule == null) {
            return;
        }

        rule.setName(dto.name() != null ? dto.name() : rule.getName());
        rule.setPriority(dto.priority() != null ? dto.priority() : rule.getPriority());
        rule.setIsActive(dto.isActive() != null ? dto.isActive() : rule.getIsActive());

        if (dto.intervalSeconds() != null) {
            rule.setIntervalSeconds(dto.intervalSeconds());
            rule.setIsInterval(dto.intervalSeconds() > 0);
        }

        if (dto.conditions() != null) {
            rule.getConditions().clear();
            dto.conditions().forEach(condDto -> {
                RuleConditionV2 condition = new RuleConditionV2();
                ruleConditionMapper.updateFromDto(condDto, condition);
                rule.addCondition(condition);
            });
        }

        if (dto.actions() != null) {
            rule.getActions().clear();
            dto.actions().forEach(actDto -> {
                RuleActionV2 action = new RuleActionV2();
                ruleActionMapper.updateFromDto(actDto, action);
                rule.addAction(action);
            });
        }
    }

    @Override
    public RuleV2Dto toDto(RuleV2 entity) {
        if (entity == null) {
            return null;
        }

        return RuleV2Dto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .priority(entity.getPriority())
                .isActive(entity.getIsActive())
                .roomId(entity.getRoomId())
                .intervalSeconds(entity.getIntervalSeconds())
                .conditions(ruleConditionMapper.toDtoList(entity.getConditions()))
                .actions(ruleActionMapper.toDtoList(entity.getActions()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public List<RuleV2Dto> toDtoList(List<RuleV2> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toDto)
                .toList();
    }
}
