package com.iviet.ivshs.service.alert.impl;

import com.iviet.ivshs.dao.AlertInstanceLogDao;
import com.iviet.ivshs.entities.AlertInstanceLog;
import com.iviet.ivshs.entities.AlertInstance;
import com.iviet.ivshs.service.alert.AlertInstanceLogService;
import com.iviet.ivshs.shared.enumeration.AlertActionType;
import com.iviet.ivshs.shared.enumeration.AlertActorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertInstanceLogServiceImpl implements AlertInstanceLogService {

    private final AlertInstanceLogDao AlertInstanceLogDao;

    @Override
    @Transactional
    public AlertInstanceLog createLog(AlertInstance alert, AlertActionType actionType, AlertActorType actorType,
            String actorId, String message) {
        AlertInstanceLog logEntity = AlertInstanceLog.builder().alert(alert).actionType(actionType).actorType(actorType)
                .actorId(actorId).message(message).createdAt(Instant.now()).build();
        AlertInstanceLogDao.save(logEntity);
        return logEntity;
    }

    @Override
    public List<AlertInstanceLog> getLogsByAlertId(Long alertId) {
        return AlertInstanceLogDao.findAllByAlertId(alertId);
    }
}
