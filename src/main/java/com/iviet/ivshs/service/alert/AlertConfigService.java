package com.iviet.ivshs.service.alert;

import com.iviet.ivshs.dto.alert.CreateAlertConfigDto;
import com.iviet.ivshs.dto.alert.UpdateAlertConfigDto;
import com.iviet.ivshs.dto.alert.AlertConfigDto;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;

import java.util.List;

public interface AlertConfigService {

    /** Lấy danh sách config theo namespace + sourceId (ví dụ: toàn bộ configs của Rule 4). */
    List<AlertConfigDto> getConfigsBySource(AlertNamespace namespace, String sourceId);

    /** Lấy tất cả config có phân trang, filter namespace optional (dùng cho trang manage độc lập). */
    PaginatedResponse<AlertConfigDto> getAllConfigs(AlertNamespace namespace, int page, int size);

    /** Lấy chi tiết một config theo ID. */
    AlertConfigDto getConfigById(Long id);

    /** Tạo mới AlertConfig. */
    AlertConfigDto createConfig(CreateAlertConfigDto dto);

    /** Cập nhật AlertConfig. */
    AlertConfigDto updateConfig(Long id, UpdateAlertConfigDto dto);

    /** Xóa một AlertConfig theo ID. */
    void deleteConfig(Long id);

    /**
     * Xóa tất cả AlertConfig theo namespace + sourceId. Gọi bởi RuleService khi xóa Rule.
     */
    void deleteAllBySource(AlertNamespace namespace, String sourceId);
}
