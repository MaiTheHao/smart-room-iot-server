  package com.iviet.ivshs.mapper.impl;

  import com.iviet.ivshs.dto.CreateRuleDto;
  import com.iviet.ivshs.dto.RuleDto;
  import com.iviet.ivshs.dto.UpdateRuleDto;
  import com.iviet.ivshs.entities.Rule;
  import com.iviet.ivshs.entities.RuleCondition;
  import com.iviet.ivshs.mapper.RuleConditionMapper;
  import com.iviet.ivshs.mapper.RuleMapper;

  import lombok.RequiredArgsConstructor;
  import org.springframework.stereotype.Component;

  import java.util.Collections;
  import java.util.List;

  @Component
  @RequiredArgsConstructor
  public class RuleMapperImpl implements RuleMapper {

    private final RuleConditionMapper ruleConditionMapper;

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
      rule.setRoomId(dto.roomId());
      rule.setTargetDeviceId(dto.targetDeviceId());
      rule.setTargetDeviceCategory(dto.targetDeviceCategory());
      rule.setActionParams(dto.actionParams());

      if (dto.conditions() != null && !dto.conditions().isEmpty()) {
        dto.conditions().forEach(condDto -> {
          RuleCondition condition = ruleConditionMapper.toEntity(condDto);
          rule.addCondition(condition);
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
      rule.setRoomId(dto.roomId());
      rule.setTargetDeviceId(dto.targetDeviceId());
      rule.setTargetDeviceCategory(dto.targetDeviceCategory());
      rule.setActionParams(dto.actionParams());
      rule.setIsActive(true);
      
      if (dto.conditions() != null) {
        dto.conditions().forEach(condDto -> {
          RuleCondition condition = ruleConditionMapper.fromCreateDto(condDto);
          rule.addCondition(condition);
        });
      } else {
        rule.setConditions(Collections.emptyList());
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
      rule.setTargetDeviceId(dto.targetDeviceId() != null ? dto.targetDeviceId() : rule.getTargetDeviceId());
      rule.setTargetDeviceCategory(dto.targetDeviceCategory() != null ? dto.targetDeviceCategory() : rule.getTargetDeviceCategory());
      rule.setActionParams(dto.actionParams() != null ? dto.actionParams() : rule.getActionParams());
      rule.setIsActive(dto.isActive() != null ? dto.isActive() : rule.getIsActive());
      rule.getConditions().clear();

      if (dto.conditions() != null) {
        dto.conditions().forEach(condDto -> {
          RuleCondition condition = new RuleCondition();
          ruleConditionMapper.updateFromDto(condDto, condition);
          rule.addCondition(condition);
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
          .roomId(entity.getRoomId())
          .targetDeviceId(entity.getTargetDeviceId())
          .targetDeviceCategory(entity.getTargetDeviceCategory())
          .actionParams(entity.getActionParams())
          .conditions(ruleConditionMapper.toDtoList(entity.getConditions()))
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
