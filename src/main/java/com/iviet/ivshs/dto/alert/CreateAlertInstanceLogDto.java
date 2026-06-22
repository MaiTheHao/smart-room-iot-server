package com.iviet.ivshs.dto.alert;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.shared.enumeration.AlertActionType;
import com.iviet.ivshs.shared.enumeration.AlertActorType;
import lombok.Builder;

@Builder
public record CreateAlertInstanceLogDto(
        Long alertId,
        AlertActionType actionType,
        AlertActorType actorType,
        String actorId,
        String message,
        JsonNode payload
) {}
