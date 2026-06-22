package com.iviet.ivshs.dto.alert;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.AlertInstanceLog;
import com.iviet.ivshs.shared.enumeration.AlertActionType;
import com.iviet.ivshs.shared.enumeration.AlertActorType;

import java.time.Instant;

public record AlertInstanceLogDto(
        Long id,
        Long alertId,
        AlertActionType actionType,
        AlertActorType actorType,
        String actorId,
        String message,
        JsonNode payload,
        Instant createdAt
) {
    public static AlertInstanceLogDto from(AlertInstanceLog log) {
        return new AlertInstanceLogDto(
                log.getId(),
                log.getAlert() != null ? log.getAlert().getId() : null,
                log.getActionType(),
                log.getActorType(),
                log.getActorId(),
                log.getMessage(),
                log.getPayload(),
                log.getCreatedAt()
        );
    }
}
