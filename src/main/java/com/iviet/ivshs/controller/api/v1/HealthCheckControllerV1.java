package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.HealthCheckResponseDto;
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
    public ResponseEntity<ApiResponse<HealthCheckResponseDto>> getClientHealth(
            @PathVariable(name = "clientId") Long clientId) {
        return ResponseEntity.ok(ApiResponse.ok(healthCheckService.checkByClient(clientId)));
    }

    @GetMapping("/clients/health")
    public ResponseEntity<ApiResponse<HealthCheckResponseDto>> getClientHealthByIp(
            @RequestParam(name = "ip") String ip) {
        return ResponseEntity.ok(ApiResponse.ok(healthCheckService.checkByClient(ip)));
    }

    @GetMapping("/clients/{clientId}/health-score")
    public ResponseEntity<ApiResponse<Integer>> getClientHealthScore(
            @PathVariable(name = "clientId") Long clientId) {
        return ResponseEntity.ok(ApiResponse.ok(healthCheckService.getHealthScoreByClient(clientId)));
    }

    @GetMapping("/rooms/{roomId}/health")
    public ResponseEntity<ApiResponse<Map<String, HealthCheckResponseDto>>> getRoomHealth(
            @PathVariable(name = "roomId") Long roomId) {
        return ResponseEntity.ok(ApiResponse.ok(healthCheckService.checkByRoom(roomId)));
    }

    @GetMapping("/rooms/health")
    public ResponseEntity<ApiResponse<Map<String, HealthCheckResponseDto>>> getRoomHealthByCode(
            @RequestParam(name = "code") String code) {
        return ResponseEntity.ok(ApiResponse.ok(healthCheckService.checkByRoom(code)));
    }

    @GetMapping("/rooms/{roomId}/health-score")
    public ResponseEntity<ApiResponse<Integer>> getRoomHealthScore(
            @PathVariable(name = "roomId") Long roomId) {
        return ResponseEntity.ok(ApiResponse.ok(healthCheckService.getHealthScoreByRoom(roomId)));
    }
}