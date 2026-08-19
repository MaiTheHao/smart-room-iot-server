package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.Action;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateActionDto(
    @NotNull(message = "Owner category cannot be null") ActionOwnerCategory ownerCategory,

    @NotBlank(message = "Owner id cannot be blank") String ownerId,

    @NotNull(message = "Target category cannot be null") DeviceCategory targetCategory,

    @NotBlank(message = "Target id cannot be blank") String targetId,

    @NotNull(message = "Action params cannot be null") JsonNode params,

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
