package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.RoomEvent;
import com.iviet.ivshs.shared.enumeration.RoomEventCode;

public record RoomEventCodeDto(Long id, RoomEventCode code, String description) {
  public static RoomEventCodeDto fromEntity(RoomEvent entity) {
    if (entity == null) {
      return null;
    }
    return new RoomEventCodeDto(entity.getId(), entity.getCode(), entity.getDescription());
  }
}
