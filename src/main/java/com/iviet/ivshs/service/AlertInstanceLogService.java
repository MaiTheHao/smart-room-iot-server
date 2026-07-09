package com.iviet.ivshs.service.alert;

import com.iviet.ivshs.dto.AlertInstanceLogDto;
import com.iviet.ivshs.dto.AlertInstanceLogFilterDto;
import com.iviet.ivshs.dto.CreateAlertInstanceLogDto;
import com.iviet.ivshs.dto.PaginatedResponse;

import java.util.List;

public interface AlertInstanceLogService {
    AlertInstanceLogDto createLog(CreateAlertInstanceLogDto createDto);

    List<AlertInstanceLogDto> getLogsByAlertId(Long alertId);

    PaginatedResponse<AlertInstanceLogDto> getLogsByAlertId(Long alertId, AlertInstanceLogFilterDto filter);

    long countLogsByAlertId(Long alertId, AlertInstanceLogFilterDto filter);
}
