package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.alert.AlertFilterDto;
import com.iviet.ivshs.dto.alert.AlertResponseDto;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.service.alert.AlertService;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;
import com.iviet.ivshs.dto.alert.UpdateAlertStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

/**
 * REST Controller cho Alert Management.
 * Tất cả endpoint RBAC được xử lý trong AlertService dựa trên group của user hiện tại.
 * Route: /api/v1/alerts (prefix /api được thêm bởi Spring Security filter hoặc global prefix config)
 */
@Slf4j
@RestController("alertController")
@RequiredArgsConstructor
@RequestMapping("/v1/alerts")
public class AlertController {

    private final AlertService alertService;

    /**
     * GET /api/v1/alerts
     * Lấy danh sách alerts với filter và phân trang.
     * RBAC: Admin → tất cả | Maintenance → của group | User → "My Alerts"
     *
     * @param status   Optional: ACTIVE | ACKNOWLEDGED | RESOLVED
     * @param severity Optional: INFO | WARNING | CRITICAL
     * @param page     Trang (0-based). Default: 0.
     * @param size     Kích thước trang. Default: 10. Max: 100.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<AlertResponseDto>>> getAlerts(
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(name = "page", defaultValue = "0")  int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        AlertFilterDto filter = new AlertFilterDto(status, severity, page, size);
        return ResponseEntity.ok(ApiResponse.ok(alertService.getAlerts(filter)));
    }

    /**
     * GET /api/v1/alerts/{id}
     * Chi tiết 1 alert. Throw 403 nếu user không có quyền xem.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertResponseDto>> getAlertById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.getAlertById(id)));
    }

    /**
     * PATCH /api/v1/alerts/{id}
     * Cập nhật trạng thái alert (Xác nhận hoặc Giải quyết).
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertResponseDto>> updateAlertStatus(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateAlertStatusDto request) {
        AlertStatus status = request.status();
        if (status == AlertStatus.ACKNOWLEDGED) {
            return ResponseEntity.ok(ApiResponse.ok(alertService.acknowledge(id)));
        } else if (status == AlertStatus.RESOLVED) {
            return ResponseEntity.ok(ApiResponse.ok(alertService.resolve(id)));
        } else {
            throw new IllegalArgumentException("Invalid status update: only ACKNOWLEDGED or RESOLVED status updates are allowed manually");
        }
    }
}
