package com.iviet.ivshs.dto.alert;

import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;

/**
 * Filter params cho GET /api/v1/alerts. Tất cả fields đều optional. Compact constructor tự validate và sanitize giá trị
 * page/size.
 */
public record AlertFilterDto(AlertStatus status, Severity severity, int page, int size) {
    public AlertFilterDto {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;
    }
}
