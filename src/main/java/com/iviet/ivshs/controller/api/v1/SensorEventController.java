package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.SensorEventRequestDto;
import com.iviet.ivshs.service.registry.EventTelemetryStrategyRegistry;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class SensorEventController {

    private final EventTelemetryStrategyRegistry eventTelemetryStrategyRegistry;

    @PostMapping("/sensors/{naturalId}/event")
    public ResponseEntity<ApiResponse<Void>> ingestSensorEvent(
            @PathVariable(name = "naturalId") String naturalId,
            @Valid @RequestBody SensorEventRequestDto request
    ) {
        eventTelemetryStrategyRegistry.processData(naturalId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
