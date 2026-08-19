package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;
import com.iviet.ivshs.shared.enumeration.ConditionLogic;
import com.iviet.ivshs.shared.enumeration.ConditionOperator;
import com.iviet.ivshs.shared.enumeration.ConditionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import lombok.Builder;

@Builder
public record CreateConditionDto(
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
    ConditionLogic nextLogic) {

  public Condition toEntity() {
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
}
