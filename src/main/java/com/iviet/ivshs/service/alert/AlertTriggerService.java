package com.iviet.ivshs.service.alert;

import com.iviet.ivshs.dto.alert.AlertTriggerRequestDto;

public interface AlertTriggerService {
    void trigger(AlertTriggerRequestDto request);
}
