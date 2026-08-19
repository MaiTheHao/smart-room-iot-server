package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.Action;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.time.Instant;
import lombok.Builder;

@Builder
public record ActionDto(
    Long id,
    ActionOwnerCategory ownerCategory,
    String ownerId,
    DeviceCategory targetCategory,
    String targetId,
    JsonNode params,
    Integer executionOrder,
    Instant createdAt,
    Instant updatedAt) {

  public static ActionDto fromEntity(Action entity) {
    if (entity == null) {
      return null;
    }
    return ActionDto.builder()
        .id(entity.getId())
        .ownerCategory(entity.getOwnerCategory())
        .ownerId(entity.getOwnerId())
        .targetCategory(entity.getTargetCategory())
        .targetId(entity.getTargetId())
        .params(entity.getParams())
        .executionOrder(entity.getExecutionOrder())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  public Action toEntity() {
    Action entity = new Action();
    entity.setId(id);
    entity.setOwnerCategory(ownerCategory);
    entity.setOwnerId(ownerId);
    entity.setTargetCategory(targetCategory);
    entity.setTargetId(targetId);
    entity.setParams(params);
    entity.setExecutionOrder(executionOrder != null ? executionOrder : 0);
    return entity;
  }
}
