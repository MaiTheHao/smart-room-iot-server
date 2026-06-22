package com.iviet.ivshs.service.alert;

import com.iviet.ivshs.dto.alert.AlertInstanceLogDto;
import com.iviet.ivshs.dto.alert.CreateAlertInstanceLogDto;
import java.util.List;

public interface AlertInstanceLogService {
    AlertInstanceLogDto createLog(CreateAlertInstanceLogDto createDto);

    List<AlertInstanceLogDto> getLogsByAlertId(Long alertId);
}
