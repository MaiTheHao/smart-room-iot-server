package com.iviet.ivshs.service.alert;

import com.iviet.ivshs.entities.AlertInstance;
import com.iviet.ivshs.entities.AlertInstanceLog;
import com.iviet.ivshs.shared.enumeration.AlertActionType;
import com.iviet.ivshs.shared.enumeration.AlertActorType;
import java.util.List;

public interface AlertInstanceLogService {
    AlertInstanceLog createLog(AlertInstance alert, AlertActionType actionType, AlertActorType actorType,
            String actorId, String message);

    List<AlertInstanceLog> getLogsByAlertId(Long alertId);
}
