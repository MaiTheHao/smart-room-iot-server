package com.iviet.ivshs.mapper.impl;

import com.iviet.ivshs.dto.rule.CreateRuleDto;
import com.iviet.ivshs.dto.rule.RuleDto;
import com.iviet.ivshs.dto.rule.UpdateRuleDto;
import com.iviet.ivshs.entities.RuleAction;
import com.iviet.ivshs.entities.RuleCondition;
import com.iviet.ivshs.mapper.RuleActionMapper;
import com.iviet.ivshs.mapper.RuleConditionMapper;
import com.iviet.ivshs.mapper.RuleMapper;
import com.iviet.ivshs.entities.Rule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RuleMapperImpl implements RuleMapper {

    private final RuleConditionMapper ruleConditionMapper;
    private final RuleActionMapper ruleActionMapper;

    @Override
    public Rule toEntity(RuleDto dto) {
        if (dto == null) {
            return null;
        }

        Rule rule = new Rule();
        rule.setId(dto.id());
        rule.setName(dto.name());
        rule.setPriority(dto.priority());
        rule.setIsActive(dto.isActive());
        rule.setIntervalSeconds(dto.intervalSeconds());
        rule.setIsInterval(dto.intervalSeconds() != null && dto.intervalSeconds() > 0);

        if (dto.conditions() != null && !dto.conditions().isEmpty()) {
            dto.conditions().forEach(condDto -> {
                RuleCondition condition = ruleConditionMapper.toEntity(condDto);
                rule.addCondition(condition);
            });
        }

        if (dto.actions() != null && !dto.actions().isEmpty()) {
            dto.actions().forEach(actDto -> {
                RuleAction action = ruleActionMapper.toEntity(actDto);
                rule.addAction(action);
            });
        }

        return rule;
    }

    @Override
    public Rule fromCreateDto(CreateRuleDto dto) {
        if (dto == null) {
            return null;
        }

        Rule rule = new Rule();
        rule.setName(dto.name());
        rule.setPriority(dto.priority());
        rule.setIntervalSeconds(dto.intervalSeconds());
        rule.setIsInterval(dto.intervalSeconds() != null && dto.intervalSeconds() > 0);
        rule.setIsActive(true);

        if (dto.conditions() != null) {
            dto.conditions().forEach(condDto -> {
                RuleCondition condition = ruleConditionMapper.fromCreateDto(condDto);
                rule.addCondition(condition);
            });
        }

        if (dto.actions() != null) {
            dto.actions().forEach(actDto -> {
                RuleAction action = ruleActionMapper.fromCreateDto(actDto);
                rule.addAction(action);
            });
        }

        return rule;
    }

    @Override
    public void updateFromDto(UpdateRuleDto dto, Rule rule) {
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
                RuleCondition condition = new RuleCondition();
                ruleConditionMapper.updateFromDto(condDto, condition);
                rule.addCondition(condition);
            });
        }

        if (dto.actions() != null) {
            rule.getActions().clear();
            dto.actions().forEach(actDto -> {
                RuleAction action = new RuleAction();
                ruleActionMapper.updateFromDto(actDto, action);
                rule.addAction(action);
            });
        }
    }

    @Override
    public RuleDto toDto(Rule entity) {
        if (entity == null) {
            return null;
        }

        return RuleDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .priority(entity.getPriority())
                .isActive(entity.getIsActive())
                .intervalSeconds(entity.getIntervalSeconds())
                .conditions(ruleConditionMapper.toDtoList(entity.getConditions()))
                .actions(ruleActionMapper.toDtoList(entity.getActions()))
                .alertConfigs(entity.getAlerts() != null ? entity.getAlerts().stream()
                        .map(com.iviet.ivshs.dto.alert.RuleActionAlertDto::from)
                        .collect(java.util.stream.Collectors.toList()) : java.util.List.of())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public List<RuleDto> toDtoList(List<Rule> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toDto)
                .toList();
    }
}
