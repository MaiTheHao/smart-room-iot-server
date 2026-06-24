package com.iviet.ivshs.service.alert;

import com.iviet.ivshs.dto.alert.AlertInstanceLogDto;
import com.iviet.ivshs.dto.alert.CreateAlertInstanceLogDto;
import com.iviet.ivshs.dto.alert.AlertInstanceLogFilterDto;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import java.util.List;

public interface AlertInstanceLogService {
    AlertInstanceLogDto createLog(CreateAlertInstanceLogDto createDto);

    List<AlertInstanceLogDto> getLogsByAlertId(Long alertId);

    PaginatedResponse<AlertInstanceLogDto> getLogsByAlertId(Long alertId, AlertInstanceLogFilterDto filter);

    long countLogsByAlertId(Long alertId, AlertInstanceLogFilterDto filter);
}
