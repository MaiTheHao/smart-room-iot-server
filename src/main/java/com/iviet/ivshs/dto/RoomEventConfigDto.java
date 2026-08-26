package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.RoomEventConfig;
import com.iviet.ivshs.shared.enumeration.RoomEventCode;
import java.time.Instant;
import lombok.Builder;

@Builder
public record RoomEventConfigDto(
    Long id,
    Long roomId,
    String roomName,
    Long roomEventId,
    RoomEventCode eventCode,
    String eventDescription,
    Boolean isActive,
    Integer cooldownSeconds,
    Instant lastTriggeredAt,
    Instant createdAt,
    Instant updatedAt
) {
  public static RoomEventConfigDto fromEntity(RoomEventConfig entity) {
    if (entity == null) return null;
    return RoomEventConfigDto.builder()
        .id(entity.getId())
        .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
        .roomName(entity.getRoom() != null && entity.getRoom().getCode() != null ? entity.getRoom().getCode() : null)
        .roomEventId(entity.getRoomEvent() != null ? entity.getRoomEvent().getId() : null)
        .eventCode(entity.getRoomEvent() != null ? entity.getRoomEvent().getCode() : null)
        .eventDescription(entity.getRoomEvent() != null ? entity.getRoomEvent().getDescription() : null)
        .isActive(entity.getIsActive())
        .cooldownSeconds(entity.getCooldownSeconds())
        .lastTriggeredAt(entity.getLastTriggeredAt())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }
}
