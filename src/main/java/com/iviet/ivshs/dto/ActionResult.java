package com.iviet.ivshs.dto;

import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import lombok.Builder;

@Builder
public record ActionResult(
    Long actionId,
    DeviceCategory targetCategory,
    String targetId,
    boolean success,
    String message,
    Object rawResponse) {

  public static ActionResult success(
      Long actionId, DeviceCategory targetCategory, String targetId, Object rawResponse) {
    return ActionResult.builder()
        .actionId(actionId)
        .targetCategory(targetCategory)
        .targetId(targetId)
        .success(true)
        .message("Executed successfully")
        .rawResponse(rawResponse)
        .build();
  }

  public static ActionResult failure(
      Long actionId, DeviceCategory targetCategory, String targetId, String errorMessage) {
    return ActionResult.builder()
        .actionId(actionId)
        .targetCategory(targetCategory)
        .targetId(targetId)
        .success(false)
        .message(errorMessage)
        .build();
  }
}
