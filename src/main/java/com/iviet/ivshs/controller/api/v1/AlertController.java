package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.alert.AlertFilterDto;
import com.iviet.ivshs.dto.alert.AlertResponseDto;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.service.alert.AlertService;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
     * POST /api/v1/alerts/{id}/acknowledge
     * Xác nhận cảnh báo.
     */
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<AlertResponseDto>> acknowledgeAlert(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.acknowledge(id)));
    }

    /**
     * POST /api/v1/alerts/{id}/resolve
     * Giải quyết cảnh báo.
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<AlertResponseDto>> resolveAlert(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.resolve(id)));
    }
}
