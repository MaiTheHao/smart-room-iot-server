package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;
import com.iviet.ivshs.shared.enumeration.ConditionLogic;
import com.iviet.ivshs.shared.enumeration.ConditionOperator;
import com.iviet.ivshs.shared.enumeration.ConditionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ReplaceConditionDto(
    Long id,
    @NotNull(message = "Source category cannot be null") ConditionDataSource sourceCategory,
    @NotBlank(message = "Source target id cannot be blank") String sourceTargetId,
    DeviceCategory sourceTargetType,
    @NotBlank(message = "Property cannot be blank") String property,
    @NotNull(message = "Operator cannot be null") ConditionOperator operator,
    @NotBlank(message = "Value cannot be blank") String value,
    JsonNode extraParams,
    Integer sortOrder,
    ConditionLogic nextLogic) {

  public Condition toEntity(ConditionOwnerCategory ownerCategory, String ownerId) {
    Condition entity = new Condition();
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

  public void updateEntity(Condition entity) {
    if (entity == null) {
      return;
    }
    if (sourceCategory != null) {
      entity.setSourceCategory(sourceCategory);
    }
    if (sourceTargetId != null) {
      entity.setSourceTargetId(sourceTargetId);
    }
    if (sourceTargetType != null) {
      entity.setSourceTargetType(sourceTargetType);
    }
    if (property != null) {
      entity.setProperty(property);
    }
    if (operator != null) {
      entity.setOperator(operator);
    }
    if (value != null) {
      entity.setValue(value);
    }
    if (extraParams != null) {
      entity.setExtraParams(extraParams);
    }
    if (sortOrder != null) {
      entity.setSortOrder(sortOrder);
    }
    if (nextLogic != null) {
      entity.setNextLogic(nextLogic);
    }
  }
}
