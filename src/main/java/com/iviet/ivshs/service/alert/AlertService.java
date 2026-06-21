package com.iviet.ivshs.service.alert;

import com.iviet.ivshs.dto.alert.AlertFilterDto;
import com.iviet.ivshs.dto.alert.AlertResponseDto;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;

public interface AlertService {

    /**
     * Gọi bởi RuleProcessor khi TẤT CẢ conditions của Rule được thỏa mãn.
     * - Kiểm tra cooldown, nếu trong cooldown: tăng trigger_count + ghi log RE_TRIGGERED.
     * - Nếu ngoài cooldown: tạo AlertRecipient mới, ghi log TRIGGERED, dispatch FCM sau commit.
     */
    void triggerAlert(Long alertConfigId);

    /**
     * Gọi bởi RuleProcessor khi conditions KHÔNG CÒN thỏa mãn.
     * Nếu auto_resolve = true: resolve toàn bộ alert đang mở, ghi log AUTO_RESOLVED.
     */
    void resolveAlertIfNeeded(Long alertConfigId);

    /** Lấy danh sách alerts phân trang, filter theo RBAC của user hiện tại (dynamic group join). */
    PaginatedResponse<AlertResponseDto> getAlerts(AlertFilterDto filter);

    /** Lấy danh sách alerts của 1 source cụ thể phân trang, filter theo RBAC của user hiện tại. */
    PaginatedResponse<AlertResponseDto> getAlertsBySource(AlertNamespace namespace, String sourceId, AlertFilterDto filter);

    /** Lấy chi tiết 1 alert. Throw ForbiddenException nếu user không có quyền. */
    AlertResponseDto getAlertById(Long alertId);

    /** Acknowledge alert. User phải có quyền F_HANDLE_ALERT và thuộc group của alert. */
    AlertResponseDto acknowledge(Long alertId);

    /** Resolve alert thủ công. User phải có quyền F_HANDLE_ALERT và thuộc group của alert. */
    AlertResponseDto resolve(Long alertId);
}
