package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.Action;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ReplaceActionDto(
    Long id,
    DeviceCategory targetCategory,
    @NotBlank(message = "Target id cannot be blank") String targetId,
    @NotNull(message = "Action params cannot be null") JsonNode params,
    Integer executionOrder) {

  public Action toEntity(ActionOwnerCategory ownerCategory, String ownerId) {
    Action entity = new Action();
    entity.setOwnerCategory(ownerCategory);
    entity.setOwnerId(ownerId);
    entity.setTargetCategory(targetCategory);
    entity.setTargetId(targetId);
    entity.setParams(params);
    entity.setExecutionOrder(executionOrder != null ? executionOrder : 0);
    return entity;
  }

  public void updateEntity(Action entity) {
    if (entity == null) {
      return;
    }
    if (targetCategory != null) {
      entity.setTargetCategory(targetCategory);
    }
    if (targetId != null) {
      entity.setTargetId(targetId);
    }
    if (params != null) {
      entity.setParams(params);
    }
    if (executionOrder != null) {
      entity.setExecutionOrder(executionOrder);
    }
  }
}
