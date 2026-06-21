package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.alert.AlertConfigDto;
import com.iviet.ivshs.dto.alert.AlertConfigResponseDto;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.service.alert.AlertConfigService;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller quản lý Alert Config.
 * Route: /api/v1/alert-configs
 */
@Slf4j
@RestController("alertConfigController")
@RequiredArgsConstructor
@RequestMapping("/v1/alert-configs")
public class AlertConfigController {

    private final AlertConfigService alertConfigService;

    /**
     * GET /api/v1/alert-configs?namespace=RULE&sourceId=4
     * Lấy danh sách configs theo namespace và sourceId.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertConfigResponseDto>>> getConfigs(
            @RequestParam AlertNamespace namespace,
            @RequestParam String sourceId) {
        return ResponseEntity.ok(ApiResponse.ok(alertConfigService.getConfigsBySource(namespace, sourceId)));
    }

    /**
     * GET /api/v1/alert-configs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertConfigResponseDto>> getConfigById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(alertConfigService.getConfigById(id)));
    }

    /**
     * POST /api/v1/alert-configs
     * Tạo mới AlertConfig.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AlertConfigResponseDto>> createConfig(@Valid @RequestBody AlertConfigDto dto) {
        return ResponseEntity.ok(ApiResponse.ok(alertConfigService.createConfig(dto)));
    }

    /**
     * PUT /api/v1/alert-configs/{id}
     * Cập nhật AlertConfig.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertConfigResponseDto>> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody AlertConfigDto dto) {
        return ResponseEntity.ok(ApiResponse.ok(alertConfigService.updateConfig(id, dto)));
    }

    /**
     * DELETE /api/v1/alert-configs/{id}
     * Xóa một AlertConfig.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long id) {
        alertConfigService.deleteConfig(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
