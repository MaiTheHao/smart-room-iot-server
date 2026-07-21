package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.AlertConfigDto;
import com.iviet.ivshs.dto.AlertConfigFilterDto;
import com.iviet.ivshs.dto.AlertFilterDto;
import com.iviet.ivshs.dto.AlertInstanceDto;
import com.iviet.ivshs.dto.AlertInstanceLogDto;
import com.iviet.ivshs.dto.AlertInstanceLogFilterDto;
import com.iviet.ivshs.dto.AlertInstanceSubFilterDto;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.CreateAlertConfigDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateAlertConfigDto;
import com.iviet.ivshs.service.AlertConfigService;
import com.iviet.ivshs.service.AlertInstanceLogService;
import com.iviet.ivshs.service.AlertInstanceService;
import com.iviet.ivshs.service.AlertTriggerService;
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
            @Valid AlertConfigFilterDto filter) {
        return ResponseEntity.ok(ApiResponse.ok(alertConfigService.getAllConfigs(filter)));
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT', 'F_ACCESS_ALERT')")
    public ResponseEntity<ApiResponse<Long>> countConfigs(@Valid AlertConfigFilterDto filter) {
        return ResponseEntity.ok(ApiResponse.ok(alertConfigService.countConfigs(filter)));
    }

    @GetMapping("/instances")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT', 'F_ACCESS_ALERT')")
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

    @GetMapping("/{alertConfigId}/instances")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT', 'F_ACCESS_ALERT')")
    public ResponseEntity<ApiResponse<PaginatedResponse<AlertInstanceDto>>> getAlertsByConfig(
            @PathVariable Long alertConfigId, @Valid AlertInstanceSubFilterDto filter) {
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceService.getAlertsByConfig(alertConfigId, filter)));
    }

    @GetMapping("/{alertConfigId}/instances/count")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT', 'F_ACCESS_ALERT')")
    public ResponseEntity<ApiResponse<Long>> countAlertsByConfig(
            @PathVariable Long alertConfigId, @Valid AlertInstanceSubFilterDto filter) {
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceService.countAlertsByConfig(alertConfigId, filter)));
    }

    @GetMapping("/{alertConfigId}/instances/{instanceId}")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT', 'F_ACCESS_ALERT')")
    public ResponseEntity<ApiResponse<AlertInstanceDto>> getAlertById(@PathVariable Long alertConfigId,
            @PathVariable Long instanceId) {
        return ResponseEntity.ok(ApiResponse.ok(validateAlertRelation(alertConfigId, instanceId)));
    }

    @PostMapping("/{alertConfigId}/instances/{instanceId}/acknowledge")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_ACCESS_ALERT')")
    public ResponseEntity<ApiResponse<AlertInstanceDto>> acknowledgeAlert(@PathVariable Long alertConfigId,
            @PathVariable Long instanceId) {
        validateAlertRelation(alertConfigId, instanceId);
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        alertTriggerService.handleAction(instanceId, AlertActionType.ACKNOWLEDGED, AlertActorType.USER,
                String.valueOf(currentClientId), null, null);
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceService.getAlertById(instanceId)));
    }

    @PostMapping("/{alertConfigId}/instances/{instanceId}/resolve")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_ACCESS_ALERT')")
    public ResponseEntity<ApiResponse<AlertInstanceDto>> resolveAlert(@PathVariable Long alertConfigId,
            @PathVariable Long instanceId) {
        validateAlertRelation(alertConfigId, instanceId);
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        alertTriggerService.handleAction(instanceId, AlertActionType.RESOLVED, AlertActorType.USER,
                String.valueOf(currentClientId), null, null);
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceService.getAlertById(instanceId)));
    }

    @GetMapping("/{alertConfigId}/instances/{instanceId}/logs")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT', 'F_ACCESS_ALERT')")
    public ResponseEntity<ApiResponse<PaginatedResponse<AlertInstanceLogDto>>> getAlertLogs(
            @PathVariable Long alertConfigId, @PathVariable Long instanceId,
            @Valid AlertInstanceLogFilterDto filter) {
        validateAlertRelation(alertConfigId, instanceId);
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceLogService.getLogsByAlertId(instanceId, filter)));
    }

    @GetMapping("/{alertConfigId}/instances/{instanceId}/logs/count")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ALERT', 'F_ACCESS_ALERT')")
    public ResponseEntity<ApiResponse<Long>> countAlertLogs(
            @PathVariable Long alertConfigId, @PathVariable Long instanceId,
            @Valid AlertInstanceLogFilterDto filter) {
        validateAlertRelation(alertConfigId, instanceId);
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceLogService.countLogsByAlertId(instanceId, filter)));
    }

    private AlertInstanceDto validateAlertRelation(Long alertConfigId, Long instanceId) {
        AlertInstanceDto alert = alertInstanceService.getAlertById(instanceId);
        if (!alert.alertConfigId().equals(alertConfigId)) {
            throw new NotFoundException(
                    "Alert instance " + instanceId + " does not belong to configuration " + alertConfigId);
        }
        return alert;
    }
}
