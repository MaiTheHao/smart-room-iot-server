package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.alert.AlertConfigDto;
import com.iviet.ivshs.dto.alert.CreateAlertConfigDto;
import com.iviet.ivshs.dto.alert.UpdateAlertConfigDto;
import com.iviet.ivshs.dto.alert.AlertFilterDto;
import com.iviet.ivshs.dto.alert.AlertInstanceDto;
import com.iviet.ivshs.dto.alert.AlertInstanceLogDto;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.service.alert.AlertConfigService;
import com.iviet.ivshs.service.alert.AlertInstanceLogService;
import com.iviet.ivshs.service.alert.AlertInstanceService;
import com.iviet.ivshs.service.alert.AlertTriggerService;
import com.iviet.ivshs.shared.enumeration.AlertActionType;
import com.iviet.ivshs.shared.enumeration.AlertActorType;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.shared.util.SecurityContextUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestController("alertController")
@RequiredArgsConstructor
@RequestMapping("/v1/alerts")
public class AlertController {

    private final AlertInstanceService alertInstanceService;
    private final AlertTriggerService alertTriggerService;
    private final AlertInstanceLogService alertInstanceLogService;
    private final AlertConfigService alertConfigService;

    @PostMapping()
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT')")
    public ResponseEntity<ApiResponse<AlertConfigDto>> createConfig(@RequestBody @Valid CreateAlertConfigDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(alertConfigService.createConfig(dto)));
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT')")
    public ResponseEntity<ApiResponse<AlertConfigDto>> updateConfig(@PathVariable Long id,
            @RequestBody @Valid UpdateAlertConfigDto dto) {
        return ResponseEntity.ok(ApiResponse.ok(alertConfigService.updateConfig(id, dto)));
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT', 'F_ACCESS_ALERT')")
    public ResponseEntity<ApiResponse<AlertConfigDto>> getConfigById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(alertConfigService.getConfigById(id)));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT')")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long id) {
        alertConfigService.deleteConfig(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Alert configuration deleted successfully"));
    }

    @GetMapping()
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT', 'F_ACCESS_ALERT')")
    public ResponseEntity<ApiResponse<PaginatedResponse<AlertConfigDto>>> getConfigs(
            @RequestParam(required = false) AlertNamespace namespace, @RequestParam(required = false) String sourceId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        if (namespace != null && sourceId != null) {
            List<AlertConfigDto> configs = alertConfigService.getConfigsBySource(namespace, sourceId);
            return ResponseEntity.ok(ApiResponse.ok(PaginatedResponse.ofList(configs, page, size)));
        }
        return ResponseEntity.ok(ApiResponse.ok(alertConfigService.getAllConfigs(namespace, page, size)));
    }

    @GetMapping("/instances")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT', 'F_ACCESS_ALERT', 'F_HANDLE_ALERT')")
    public ResponseEntity<ApiResponse<PaginatedResponse<AlertInstanceDto>>> getAlerts(
            @RequestParam(required = false) AlertStatus status, @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) AlertNamespace namespace,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        AlertFilterDto filter = new AlertFilterDto(status, severity, namespace, from, to, page, size);
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceService.getAlerts(filter)));
    }

    @GetMapping("/{alertId}/instances")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT', 'F_ACCESS_ALERT', 'F_HANDLE_ALERT')")
    public ResponseEntity<ApiResponse<PaginatedResponse<AlertInstanceDto>>> getAlertsByConfig(
            @PathVariable Long alertId, @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        AlertFilterDto filter = new AlertFilterDto(status, severity, null, null, null, page, size);
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceService.getAlertsByConfig(alertId, filter)));
    }

    @GetMapping("/{alertId}/instances/{instanceId}")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT', 'F_ACCESS_ALERT', 'F_HANDLE_ALERT')")
    public ResponseEntity<ApiResponse<AlertInstanceDto>> getAlertById(@PathVariable Long alertId,
            @PathVariable Long instanceId) {
        return ResponseEntity.ok(ApiResponse.ok(validateAlertRelation(alertId, instanceId)));
    }

    @PostMapping("/{alertId}/instances/{instanceId}/acknowledge")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_HANDLE_ALERT')")
    public ResponseEntity<ApiResponse<AlertInstanceDto>> acknowledgeAlert(@PathVariable Long alertId,
            @PathVariable Long instanceId) {
        validateAlertRelation(alertId, instanceId);
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        alertTriggerService.handleAction(instanceId, AlertActionType.ACKNOWLEDGED, AlertActorType.USER,
                String.valueOf(currentClientId), null, null);
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceService.getAlertById(instanceId)));
    }

    @PostMapping("/{alertId}/instances/{instanceId}/resolve")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_HANDLE_ALERT')")
    public ResponseEntity<ApiResponse<AlertInstanceDto>> resolveAlert(@PathVariable Long alertId,
            @PathVariable Long instanceId) {
        validateAlertRelation(alertId, instanceId);
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        alertTriggerService.handleAction(instanceId, AlertActionType.RESOLVED, AlertActorType.USER,
                String.valueOf(currentClientId), null, null);
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceService.getAlertById(instanceId)));
    }

    @GetMapping("/{alertId}/instances/{instanceId}/logs")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT', 'F_ACCESS_ALERT', 'F_HANDLE_ALERT')")
    public ResponseEntity<ApiResponse<List<AlertInstanceLogDto>>> getAlertLogs(@PathVariable Long alertId,
            @PathVariable Long instanceId) {
        validateAlertRelation(alertId, instanceId);
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceLogService.getLogsByAlertId(instanceId)));
    }

    private AlertInstanceDto validateAlertRelation(Long alertId, Long instanceId) {
        AlertInstanceDto alert = alertInstanceService.getAlertById(instanceId);
        if (!alert.alertConfigId().equals(alertId)) {
            throw new NotFoundException(
                    "Alert instance " + instanceId + " does not belong to configuration " + alertId);
        }
        return alert;
    }
}
