package com.iviet.ivshs.service.alert.impl;

import com.iviet.ivshs.dao.AlertInstanceLogDao;
import com.iviet.ivshs.dto.alert.AlertInstanceLogDto;
import com.iviet.ivshs.dto.alert.CreateAlertInstanceLogDto;
import com.iviet.ivshs.entities.AlertInstance;
import com.iviet.ivshs.entities.AlertInstanceLog;
import com.iviet.ivshs.service.alert.AlertInstanceLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertInstanceLogServiceImpl implements AlertInstanceLogService {

    private final AlertInstanceLogDao alertInstanceLogDao;

    @Override
    @Transactional
    public AlertInstanceLogDto createLog(CreateAlertInstanceLogDto createDto) {
        AlertInstance alert = new AlertInstance();
        alert.setId(createDto.alertId());

        AlertInstanceLog logEntity = AlertInstanceLog.builder()
                .alert(alert)
                .actionType(createDto.actionType())
                .actorType(createDto.actorType())
                .actorId(createDto.actorId())
                .message(createDto.message())
                .payload(createDto.payload())
                .createdAt(Instant.now())
                .build();
        alertInstanceLogDao.save(logEntity);
        return AlertInstanceLogDto.from(logEntity);
    }

    @Override
    public List<AlertInstanceLogDto> getLogsByAlertId(Long alertId) {
        return alertInstanceLogDao.findAllByAlertId(alertId).stream()
                .map(AlertInstanceLogDto::from)
                .collect(Collectors.toList());
    }
}
