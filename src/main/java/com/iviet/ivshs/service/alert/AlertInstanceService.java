package com.iviet.ivshs.service.alert;

import java.util.Map;

import com.iviet.ivshs.dto.AlertFilterDto;
import com.iviet.ivshs.dto.AlertInstanceDto;
import com.iviet.ivshs.dto.AlertInstanceSubFilterDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.entities.AlertInstance;
import com.iviet.ivshs.shared.enumeration.AlertActorType;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.enumeration.AlertStatus;

public interface AlertInstanceService {

    AlertInstance createActiveAlert(AlertConfig config, Map<String, Object> messageTemplateData);

    AlertInstance incrementTriggerCount(Long alertId);

    AlertInstance updateStatus(Long alertId, AlertStatus status, AlertActorType actorType, String actorId);

    PaginatedResponse<AlertInstanceDto> getAlerts(AlertFilterDto filter);

    PaginatedResponse<AlertInstanceDto> getAlertsBySource(AlertNamespace namespace, String sourceId,
            AlertFilterDto filter);

    AlertInstanceDto getAlertById(Long alertId);

    PaginatedResponse<AlertInstanceDto> getAlertsByConfig(Long alertConfigId, AlertFilterDto filter);

    PaginatedResponse<AlertInstanceDto> getAlertsByConfig(Long alertConfigId, AlertInstanceSubFilterDto filter);

    long countAlertsByConfig(Long alertConfigId, AlertInstanceSubFilterDto filter);
}
