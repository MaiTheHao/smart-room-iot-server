package com.iviet.ivshs.controller.api.v1;

import java.time.Instant;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.service.powerconsumption.PowerConsumptionService;
import com.iviet.ivshs.service.temperature.TemperatureService;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.service.permission.PermissionService;
import com.iviet.ivshs.service.telemetry.SensorTelemetryOrchestratorService;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class SensorTelemetryController {

    private final SensorTelemetryOrchestratorService orchestratorService;
    private final PermissionService permissionService;
    private final TemperatureService temperatureService;
    private final PowerConsumptionService powerConsumptionService;

    @GetMapping("/sensors/{sensorId}/history")
    public ResponseEntity<ApiResponse<List<?>>> getHistory(
        @PathVariable(name = "sensorId") Long sensorId,
        @RequestParam(name = "category") DeviceCategory category,
        @RequestParam(name = "from") Instant from,
        @RequestParam(name = "to") Instant to
    ) {
        Long roomId = resolveRoomId(sensorId, null, category);
        permissionService.requireAccessRoom(roomId);

        List<?> history = orchestratorService.getHistory(sensorId, category, from, to);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    @GetMapping("/sensors/natural/{naturalId}/history")
    public ResponseEntity<ApiResponse<List<?>>> getHistoryByNaturalId(
        @PathVariable(name = "naturalId") String naturalId,
        @RequestParam(name = "category") DeviceCategory category,
        @RequestParam(name = "from") Instant from,
        @RequestParam(name = "to") Instant to
    ) {
        Long roomId = resolveRoomId(null, naturalId, category);
        permissionService.requireAccessRoom(roomId);

        List<?> history = orchestratorService.getHistoryByNaturalId(naturalId, category, from, to);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    private Long resolveRoomId(Long sensorId, String naturalId, DeviceCategory category) {
        if (category == null) {
            throw new BadRequestException("Category query parameter is required");
        }
        if (sensorId != null) {
            return switch (category) {
                case TEMPERATURE -> temperatureService.getById(sensorId).roomId();
                case POWER_CONSUMPTION -> powerConsumptionService.getById(sensorId).roomId();
                default -> throw new BadRequestException("Invalid sensor category: " + category);
            };
        } else if (naturalId != null) {
            return switch (category) {
                case TEMPERATURE -> temperatureService.getByNaturalId(naturalId).roomId();
                case POWER_CONSUMPTION -> powerConsumptionService.getByNaturalId(naturalId).roomId();
                default -> throw new BadRequestException("Invalid sensor category: " + category);
            };
        }
        throw new BadRequestException("Sensor identifier is missing");
    }
}
