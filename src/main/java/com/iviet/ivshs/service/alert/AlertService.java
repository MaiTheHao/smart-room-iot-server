package com.iviet.ivshs.service.alert;

import com.iviet.ivshs.dto.alert.AlertFilterDto;
import com.iviet.ivshs.dto.alert.AlertResponseDto;
import com.iviet.ivshs.dto.alert.RuleActionAlertDto;
import com.iviet.ivshs.dto.alert.SaveRuleActionAlertDto;
import com.iviet.ivshs.dto.common.PaginatedResponse;

/**
 * Service quản lý toàn bộ vòng đời Alert:
 * - Kích hoạt alert khi Rule conditions được thỏa mãn (gọi từ RuleProcessor)
 * - Tự động resolve alert khi conditions không còn thỏa mãn
 * - RBAC-aware query cho REST API
 * - Thủ công acknowledge/resolve qua REST API
 */
public interface AlertService {

    /**
     * Lấy cấu hình cảnh báo của một Rule.
     * @param ruleId ID của Rule.
     * @return Danh sách RuleActionAlertDto.
     */
    java.util.List<RuleActionAlertDto> getAlertConfigsByRuleId(Long ruleId);

    /**
     * Lưu/Cập nhật cấu hình cảnh báo của một Rule.
     * @param ruleId ID của Rule.
     * @param dtos Danh sách SaveRuleActionAlertDto chứa thông tin cấu hình cảnh báo.
     * @return Danh sách RuleActionAlertDto đã được lưu.
     */
    java.util.List<RuleActionAlertDto> saveAlertConfigs(Long ruleId, java.util.List<SaveRuleActionAlertDto> dtos);

    /**
     * Xóa cấu hình cảnh báo của một Rule.
     * @param ruleId ID của Rule.
     */
    void deleteAlertsByRuleId(Long ruleId);

    /**
     * Gọi bởi RuleProcessor khi TẤT CẢ conditions của Rule được thỏa mãn.
     * Tạo AlertInstance mới nếu không trong cooldown, resolve recipients, dispatch notification.
     * @param alertConfigId ID của cấu hình Alert vừa match.
     */
    void triggerAlert(Long alertConfigId);

    /**
     * Gọi bởi RuleProcessor khi conditions KHÔNG CÒN thỏa mãn.
     * Nếu auto_resolve = true: cập nhật tất cả ACTIVE/ACKNOWLEDGED alerts của Rule này sang RESOLVED.
     * @param alertConfigId ID của cấu hình Alert không match.
     */
    void resolveAlertIfNeeded(Long alertConfigId);

    /**
     * Lấy danh sách alerts phân trang, filter theo RBAC của user hiện tại.
     * G_ADMIN → xem tất cả | G_MAINTENANCE → xem group | G_USER → "My Alerts"
     */
    PaginatedResponse<AlertResponseDto> getAlerts(AlertFilterDto filter);

    /**
     * Lấy danh sách alerts của riêng 1 Rule phân trang, filter theo RBAC của user hiện tại.
     */
    PaginatedResponse<AlertResponseDto> getAlertsByRuleId(Long ruleId, AlertFilterDto filter);

    /**
     * Lấy chi tiết 1 alert theo ID. Throw ForbiddenException nếu không có quyền.
     */
    AlertResponseDto getAlertById(Long alertId);

    /**
     * Đánh dấu ACKNOWLEDGED bởi user hiện tại. No-op nếu đã ACKNOWLEDGED/RESOLVED.
     */
    AlertResponseDto acknowledge(Long alertId);

    /**
     * Đánh dấu RESOLVED bởi user hiện tại. Dispatch ALERT_RESOLVED notification.
     * No-op nếu đã RESOLVED.
     */
    AlertResponseDto resolve(Long alertId);
}

