package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;
import com.iviet.ivshs.shared.enumeration.ConditionLogic;
import com.iviet.ivshs.shared.enumeration.ConditionOperator;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import lombok.Builder;

@Builder
public record UpdateConditionDto(
    ConditionDataSource sourceCategory,
    String sourceTargetId,
    DeviceCategory sourceTargetType,
    String property,
    ConditionOperator operator,
    String value,
    JsonNode extraParams,
    Integer sortOrder,
    ConditionLogic nextLogic) {

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
