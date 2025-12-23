package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.PowerConsumptionServiceV1;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PowerConsumptionControllerV1 {

    @Autowired
    private PowerConsumptionServiceV1 powerConsumptionService;

    // --- CRUD SENSOR ---

    @GetMapping("/rooms/{roomId}/power-consumptions")
    public ResponseEntity<ApiResponseV1<PaginatedResponseV1<PowerConsumptionDtoV1>>> 
            getListByRoom(
                @PathVariable(name = "roomId") Long roomId,
                @RequestParam(name = "page", defaultValue = "0") int page,
                @RequestParam(name = "size", defaultValue = "20") int size) {
        PaginatedResponseV1<PowerConsumptionDtoV1> paginated = 
            powerConsumptionService.getListByRoom(roomId, page, size);
        return ResponseEntity.ok(ApiResponseV1.ok(paginated));
    }

    @GetMapping("/power-consumptions/{id}")
    public ResponseEntity<ApiResponseV1<PowerConsumptionDtoV1>> getById(
            @PathVariable(name = "id") Long id) {
        PowerConsumptionDtoV1 dto = powerConsumptionService.getById(id);
        return ResponseEntity.ok(ApiResponseV1.ok(dto));
    }

    @PostMapping("/power-consumptions")
    public ResponseEntity<ApiResponseV1<PowerConsumptionDtoV1>> create(
            @RequestBody @Valid CreatePowerConsumptionDtoV1 dto) {
        PowerConsumptionDtoV1 created = powerConsumptionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponseV1.created(created));
    }

    @PutMapping("/power-consumptions/{id}")
    public ResponseEntity<ApiResponseV1<PowerConsumptionDtoV1>> update(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdatePowerConsumptionDtoV1 dto) {
        PowerConsumptionDtoV1 updated = powerConsumptionService.update(id, dto);
        return ResponseEntity.ok(ApiResponseV1.ok(updated));
    }

    @DeleteMapping("/power-consumptions/{id}")
    public ResponseEntity<ApiResponseV1<Void>> delete(
            @PathVariable(name = "id") Long id) {
        powerConsumptionService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(ApiResponseV1.success(HttpStatus.NO_CONTENT, null, 
                "Deleted successfully"));
    }

    // --- DATA INGESTION ---

    @PostMapping("/power-consumptions/{id}/values")
    public ResponseEntity<ApiResponseV1<Void>> ingestData(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid CreatePowerConsumptionValueDtoV1 dto) {
        powerConsumptionService.ingestSensorData(id, dto);
        return ResponseEntity.ok(ApiResponseV1.ok(null));
    }

    @PostMapping("/power-consumptions/{id}/values:batch")
    public ResponseEntity<ApiResponseV1<Void>> ingestDataBatch(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid List<CreatePowerConsumptionValueDtoV1> dtos) {
        powerConsumptionService.ingestSensorDataBatch(id, dtos);
        return ResponseEntity.ok(ApiResponseV1.ok(null));
    }

    // --- HISTORY & STATISTICS ---

    @GetMapping("/rooms/{roomId}/power-consumptions/average-history")
    public ResponseEntity<ApiResponseV1<List<AveragePowerConsumptionValueDtoV1>>> 
            getAverageHistoryByRoom(
                @PathVariable(name = "roomId") Long roomId,
                @RequestParam(name = "startedAt") 
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startedAt,
                @RequestParam(name = "endedAt") 
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endedAt) {
        List<AveragePowerConsumptionValueDtoV1> history = 
            powerConsumptionService.getAverageValueHistoryByRoomId(roomId, startedAt, 
                endedAt);
        return ResponseEntity.ok(ApiResponseV1.ok(history));
    }

    @GetMapping("/rooms/{roomId}/power-consumptions/sum-history")
    public ResponseEntity<ApiResponseV1<List<SumPowerConsumptionValueDtoV1>>> 
            getSumHistoryByRoom(
                @PathVariable(name = "roomId") Long roomId,
                @RequestParam(name = "startedAt") 
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startedAt,
                @RequestParam(name = "endedAt") 
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endedAt) {
        List<SumPowerConsumptionValueDtoV1> history = 
            powerConsumptionService.getSumValueHistoryByRoomId(roomId, startedAt, endedAt);
        return ResponseEntity.ok(ApiResponseV1.ok(history));
    }

    @DeleteMapping("/power-consumptions/{id}/values")
    public ResponseEntity<ApiResponseV1<Integer>> cleanupData(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "startedAt") 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startedAt,
            @RequestParam(name = "endedAt") 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endedAt) {
        int count = powerConsumptionService.cleanupDataByRange(id, startedAt, endedAt);
        return ResponseEntity.ok(ApiResponseV1.ok(count));
    }

    // --- UTILITY ---

    @GetMapping("/power-consumptions/{id}/health-check")
    public ResponseEntity<ApiResponseV1<HealthCheckResponseDtoV1>> healthCheck(
        @PathVariable(name = "id") Long id) {
        HealthCheckResponseDtoV1 response = powerConsumptionService.healthCheck(id);
        return ResponseEntity.ok(ApiResponseV1.ok(response));
    }
}
