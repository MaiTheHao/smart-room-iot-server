package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.alert.AlertFilterDto;
import com.iviet.ivshs.dto.alert.AlertInstanceDto;
import com.iviet.ivshs.dto.alert.AlertTriggerRequestDto;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.entities.AlertInstanceLog;
import com.iviet.ivshs.service.alert.AlertInstanceLogService;
import com.iviet.ivshs.service.alert.AlertInstanceService;
import com.iviet.ivshs.service.alert.AlertTriggerService;
import com.iviet.ivshs.shared.enumeration.AlertActionType;
import com.iviet.ivshs.shared.enumeration.AlertActorType;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;
import com.iviet.ivshs.shared.util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("alertController")
@RequiredArgsConstructor
@RequestMapping("/v1/alerts")
public class AlertController {

    private final AlertInstanceService alertInstanceService;
    private final AlertTriggerService alertTriggerService;
    private final AlertInstanceLogService AlertInstanceLogService;

    @GetMapping("/instances")
    public ResponseEntity<ApiResponse<PaginatedResponse<AlertInstanceDto>>> getAlerts(
            @RequestParam(required = false) AlertStatus status, @RequestParam(required = false) Severity severity,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        AlertFilterDto filter = new AlertFilterDto(status, severity, page, size);
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceService.getAlerts(filter)));
    }

    @GetMapping("/{alertId}/instances")
    public ResponseEntity<ApiResponse<PaginatedResponse<AlertInstanceDto>>> getAlertsByConfig(
            @PathVariable Long alertId, @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        AlertFilterDto filter = new AlertFilterDto(status, severity, page, size);
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceService.getAlertsByConfig(alertId, filter)));
    }

    @GetMapping("/{alertId}/instances/{instanceId}")
    public ResponseEntity<ApiResponse<AlertInstanceDto>> getAlertById(@PathVariable Long alertId,
            @PathVariable Long instanceId) {
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceService.getAlertById(instanceId)));
    }

    @PostMapping("/{alertId}/instances/{instanceId}/acknowledge")
    public ResponseEntity<ApiResponse<AlertInstanceDto>> acknowledgeAlert(@PathVariable Long alertId,
            @PathVariable Long instanceId) {
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        AlertTriggerRequestDto request = AlertTriggerRequestDto.builder().alertInstanceId(instanceId)
                .actionType(AlertActionType.ACKNOWLEDGED).actorType(AlertActorType.USER)
                .actorId(String.valueOf(currentClientId)).build();
        alertTriggerService.trigger(request);
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceService.getAlertById(instanceId)));
    }

    @PostMapping("/{alertId}/instances/{instanceId}/resolve")
    public ResponseEntity<ApiResponse<AlertInstanceDto>> resolveAlert(@PathVariable Long alertId,
            @PathVariable Long instanceId) {
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        AlertTriggerRequestDto request = AlertTriggerRequestDto.builder().alertInstanceId(instanceId)
                .actionType(AlertActionType.RESOLVED).actorType(AlertActorType.USER)
                .actorId(String.valueOf(currentClientId)).build();
        alertTriggerService.trigger(request);
        return ResponseEntity.ok(ApiResponse.ok(alertInstanceService.getAlertById(instanceId)));
    }

    @GetMapping("/{alertId}/instances/{instanceId}/logs")
    public ResponseEntity<ApiResponse<List<AlertInstanceLog>>> getAlertLogs(@PathVariable Long alertId,
            @PathVariable Long instanceId) {
        return ResponseEntity.ok(ApiResponse.ok(AlertInstanceLogService.getLogsByAlertId(instanceId)));
    }
}
