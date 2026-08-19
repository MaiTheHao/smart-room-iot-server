package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;
import com.iviet.ivshs.shared.enumeration.ConditionLogic;
import com.iviet.ivshs.shared.enumeration.ConditionOperator;
import com.iviet.ivshs.shared.enumeration.ConditionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.time.Instant;
import lombok.Builder;

@Builder
public record ConditionDto(
    Long id,
    ConditionOwnerCategory ownerCategory,
    String ownerId,
    ConditionDataSource sourceCategory,
    String sourceTargetId,
    DeviceCategory sourceTargetType,
    String property,
    ConditionOperator operator,
    String value,
    JsonNode extraParams,
    Integer sortOrder,
    ConditionLogic nextLogic,
    Instant createdAt,
    Instant updatedAt) {

  public static ConditionDto fromEntity(Condition entity) {
    if (entity == null) {
      return null;
    }
    return ConditionDto.builder()
        .id(entity.getId())
        .ownerCategory(entity.getOwnerCategory())
        .ownerId(entity.getOwnerId())
        .sourceCategory(entity.getSourceCategory())
        .sourceTargetId(entity.getSourceTargetId())
        .sourceTargetType(entity.getSourceTargetType())
        .property(entity.getProperty())
        .operator(entity.getOperator())
        .value(entity.getValue())
        .extraParams(entity.getExtraParams())
        .sortOrder(entity.getSortOrder())
        .nextLogic(entity.getNextLogic())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  public Condition toEntity() {
    Condition entity = new Condition();
    entity.setId(id);
    entity.setOwnerCategory(ownerCategory);
    entity.setOwnerId(ownerId);
    entity.setSourceCategory(sourceCategory);
    entity.setSourceTargetId(sourceTargetId);
    entity.setSourceTargetType(sourceTargetType);
    entity.setProperty(property);
    entity.setOperator(operator);
    entity.setValue(value);
    entity.setExtraParams(extraParams);
    entity.setSortOrder(sortOrder != null ? sortOrder : 0);
    entity.setNextLogic(nextLogic);
    return entity;
  }
}
