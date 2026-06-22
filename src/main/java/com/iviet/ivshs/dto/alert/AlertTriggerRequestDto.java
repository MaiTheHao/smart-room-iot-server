package com.iviet.ivshs.dto.alert;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.shared.enumeration.AlertActionType;
import com.iviet.ivshs.shared.enumeration.AlertActorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertTriggerRequestDto {
    /**
     * Direct reference to the alert configuration entity.
     * If provided, this configuration will be used directly.
     */
    private AlertConfig alertConfig;

    /**
     * ID of the alert configuration.
     * Fallback lookup if alertConfig is null.
     */
    private Long alertConfigId;

    private AlertActionType actionType;
    private AlertActorType actorType;
    private String actorId;

    private Map<String, Object> templateData;
    private String logMessage;
    private JsonNode payload;
}
