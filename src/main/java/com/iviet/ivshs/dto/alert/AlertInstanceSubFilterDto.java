package com.iviet.ivshs.dto.alert;

import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;

import java.time.Instant;

public record AlertInstanceSubFilterDto(AlertStatus status, Severity severity, Instant from, Instant to, int page, int size) {
    public AlertInstanceSubFilterDto {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
    }
}
