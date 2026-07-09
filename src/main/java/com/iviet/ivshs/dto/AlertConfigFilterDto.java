package com.iviet.ivshs.dto;

import com.iviet.ivshs.shared.enumeration.AlertNamespace;

public record AlertConfigFilterDto(AlertNamespace namespace, String alertCode, String sourceId, int page, int size) {
    public AlertConfigFilterDto {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
    }
}
