package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.Action;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import lombok.Builder;

@Builder
public record CreateActionDto(
    ActionOwnerCategory ownerCategory,
    String ownerId,
    DeviceCategory targetCategory,
    String targetId,
    JsonNode params,
    Integer executionOrder) {

  public Action toEntity() {
    Action entity = new Action();
    entity.setOwnerCategory(ownerCategory);
    entity.setOwnerId(ownerId);
    entity.setTargetCategory(targetCategory);
    entity.setTargetId(targetId);
    entity.setParams(params);
    entity.setExecutionOrder(executionOrder != null ? executionOrder : 0);
    return entity;
  }
}
