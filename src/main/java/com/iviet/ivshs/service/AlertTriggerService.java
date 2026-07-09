package com.iviet.ivshs.service.alert;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dto.AlertTriggerRequestDto;
import com.iviet.ivshs.shared.enumeration.AlertActionType;
import com.iviet.ivshs.shared.enumeration.AlertActorType;

public interface AlertTriggerService {
    void trigger(AlertTriggerRequestDto request);

    void handleAction(Long alertInstanceId, AlertActionType actionType, AlertActorType actorType, String actorId,
            String logMessage, JsonNode payload);
}
