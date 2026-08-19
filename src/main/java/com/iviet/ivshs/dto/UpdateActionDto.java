package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.Action;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import lombok.Builder;

@Builder
public record UpdateActionDto(
    DeviceCategory targetCategory, String targetId, JsonNode params, Integer executionOrder) {

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
