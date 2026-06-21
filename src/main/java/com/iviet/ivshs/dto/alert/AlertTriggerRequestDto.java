package com.iviet.ivshs.dto.alert;

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
    private AlertConfig alertConfig;
    private Long alertConfigId;
    private Long alertInstanceId;

    private AlertActionType actionType;
    private AlertActorType actorType;
    private String actorId;

    private Map<String, Object> templateData;
    private String customMessage;
}
