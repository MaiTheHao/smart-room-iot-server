package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.TemperatureServiceV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TemperatureControllerV1 {

    private final TemperatureServiceV1 temperatureService;

    // --- CRUD SENSOR ---

    @GetMapping("/rooms/{roomId}/temperatures")
    public ResponseEntity<ApiResponseV1<PaginatedResponseV1<TemperatureDtoV1>>> getListByRoom(
            @PathVariable(name = "roomId") Long roomId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(temperatureService.getListByRoom(roomId, page, size)));
    }

    @GetMapping("/temperatures/{id}")
    public ResponseEntity<ApiResponseV1<TemperatureDtoV1>> getById(
            @PathVariable(name = "id") Long id) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(temperatureService.getById(id)));
    }

    @PostMapping("/temperatures")
    public ResponseEntity<ApiResponseV1<TemperatureDtoV1>> create(
            @RequestBody @Valid CreateTemperatureDtoV1 dto) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseV1.created(temperatureService.create(dto)));
    }

    @PutMapping("/temperatures/{id}")
    public ResponseEntity<ApiResponseV1<TemperatureDtoV1>> update(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateTemperatureDtoV1 dto) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(temperatureService.update(id, dto)));
    }

    @DeleteMapping("/temperatures/{id}")
    public ResponseEntity<ApiResponseV1<Void>> delete(@PathVariable(name = "id") Long id) {
        temperatureService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponseV1.success(HttpStatus.NO_CONTENT, null, "Deleted successfully"));
    }

    // --- DATA INGESTION ---

    @PostMapping("/temperatures/{id}/values")
    public ResponseEntity<ApiResponseV1<Void>> ingestData(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid CreateTemperatureValueDtoV1 dto) {
        
        temperatureService.ingestSensorData(id, dto);
        return ResponseEntity.ok(ApiResponseV1.ok(null));
    }

    @PostMapping("/temperatures/{id}/values:batch")
    public ResponseEntity<ApiResponseV1<Void>> ingestDataBatch(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid List<CreateTemperatureValueDtoV1> dtos) {
        
        temperatureService.ingestSensorDataBatch(id, dtos);
        return ResponseEntity.ok(ApiResponseV1.ok(null));
    }

    // --- HISTORY ---

    @GetMapping("/rooms/{roomId}/temperatures/average-history")
    public ResponseEntity<ApiResponseV1<List<AverageTemperatureValueDtoV1>>> getAverageHistoryByRoom(
            @PathVariable(name = "roomId") Long roomId,
            @RequestParam(name = "startedAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startedAt,
            @RequestParam(name = "endedAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endedAt) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(temperatureService.getAverageValueHistoryByRoomId(roomId, startedAt, endedAt)));
    }

    @GetMapping("/client/{clientId}/temperatures/average-history")
    public ResponseEntity<ApiResponseV1<List<AverageTemperatureValueDtoV1>>> getAverageHistoryByClient(
            @PathVariable(name = "clientId") Long clientId,
            @RequestParam(name = "startedAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startedAt,
            @RequestParam(name = "endedAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endedAt) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(temperatureService.getAverageValueHistoryByClientId(clientId, startedAt, endedAt)));
    }

    @DeleteMapping("/temperatures/{id}/values")
    public ResponseEntity<ApiResponseV1<Integer>> cleanupData(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "startedAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startedAt,
            @RequestParam(name = "endedAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endedAt) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(temperatureService.cleanupDataByRange(id, startedAt, endedAt)));
    }

    // --- UTILITY ---

    @GetMapping("/temperatures/{id}/health-check")
    public ResponseEntity<ApiResponseV1<HealthCheckResponseDtoV1>> healthCheck(
            @PathVariable(name = "id") Long id) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(temperatureService.healthCheck(id)));
    }
}