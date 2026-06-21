package com.iviet.ivshs.service.alert;

import com.iviet.ivshs.dto.alert.AlertConfigDto;
import com.iviet.ivshs.dto.alert.AlertConfigResponseDto;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;

import java.util.List;

public interface AlertConfigService {

    /** Lấy danh sách config theo namespace + sourceId (ví dụ: toàn bộ configs của Rule 4). */
    List<AlertConfigResponseDto> getConfigsBySource(AlertNamespace namespace, String sourceId);

    /** Lấy chi tiết một config theo ID. */
    AlertConfigResponseDto getConfigById(Long id);

    /** Tạo mới AlertConfig. */
    AlertConfigResponseDto createConfig(AlertConfigDto dto);

    /** Cập nhật AlertConfig. */
    AlertConfigResponseDto updateConfig(Long id, AlertConfigDto dto);

    /** Xóa một AlertConfig theo ID. */
    void deleteConfig(Long id);

    /**
     * Xóa tất cả AlertConfig theo namespace + sourceId.
     * Gọi bởi RuleService khi xóa Rule.
     */
    void deleteAllBySource(AlertNamespace namespace, String sourceId);
}
