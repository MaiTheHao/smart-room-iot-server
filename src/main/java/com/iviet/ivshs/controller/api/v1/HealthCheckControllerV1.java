package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.ApiResponseV1;
import com.iviet.ivshs.dto.HealthCheckResponseDtoV1;
import com.iviet.ivshs.service.HealthCheckServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class HealthCheckControllerV1 {

    private final HealthCheckServiceV1 healthCheckService;

    @GetMapping("/clients/{clientId}/health")
    public ResponseEntity<ApiResponseV1<HealthCheckResponseDtoV1>> getClientHealth(
            @PathVariable(name = "clientId") Long clientId) {
        return ResponseEntity.ok(ApiResponseV1.ok(healthCheckService.checkByClient(clientId)));
    }

    @GetMapping("/clients/health")
    public ResponseEntity<ApiResponseV1<HealthCheckResponseDtoV1>> getClientHealthByIp(
            @RequestParam(name = "ip") String ip) {
        return ResponseEntity.ok(ApiResponseV1.ok(healthCheckService.checkByClient(ip)));
    }

    @GetMapping("/clients/{clientId}/health-score")
    public ResponseEntity<ApiResponseV1<Integer>> getClientHealthScore(
            @PathVariable(name = "clientId") Long clientId) {
        return ResponseEntity.ok(ApiResponseV1.ok(healthCheckService.getHealthScoreByClient(clientId)));
    }

    @GetMapping("/rooms/{roomId}/health")
    public ResponseEntity<ApiResponseV1<Map<String, HealthCheckResponseDtoV1>>> getRoomHealth(
            @PathVariable(name = "roomId") Long roomId) {
        return ResponseEntity.ok(ApiResponseV1.ok(healthCheckService.checkByRoom(roomId)));
    }

    @GetMapping("/rooms/health")
    public ResponseEntity<ApiResponseV1<Map<String, HealthCheckResponseDtoV1>>> getRoomHealthByCode(
            @RequestParam(name = "code") String code) {
        return ResponseEntity.ok(ApiResponseV1.ok(healthCheckService.checkByRoom(code)));
    }

    @GetMapping("/rooms/{roomId}/health-score")
    public ResponseEntity<ApiResponseV1<Integer>> getRoomHealthScore(
            @PathVariable(name = "roomId") Long roomId) {
        return ResponseEntity.ok(ApiResponseV1.ok(healthCheckService.getHealthScoreByRoom(roomId)));
    }
}