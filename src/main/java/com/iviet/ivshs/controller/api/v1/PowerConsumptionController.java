package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.PowerConsumptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PowerConsumptionController {

    private final PowerConsumptionService powerConsumptionService;

    @GetMapping("/power-consumptions")
    public ResponseEntity<ApiResponse<PaginatedResponse<PowerConsumptionDto>>> getList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(powerConsumptionService.getList(page, size)));
    }

    @GetMapping("/power-consumptions/all")
    public ResponseEntity<ApiResponse<java.util.List<PowerConsumptionDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(powerConsumptionService.getAll()));
    }

    @GetMapping("/rooms/{roomId}/power-consumptions")
    public ResponseEntity<ApiResponse<PaginatedResponse<PowerConsumptionDto>>> getListByRoomId(
            @PathVariable(name = "roomId") Long roomId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(powerConsumptionService.getListByRoomId(roomId, page, size)));
    }

    @GetMapping("/rooms/{roomId}/power-consumptions/all")
    public ResponseEntity<ApiResponse<java.util.List<PowerConsumptionDto>>> getAllByRoomId(
            @PathVariable(name = "roomId") Long roomId) {
        
        return ResponseEntity.ok(ApiResponse.ok(powerConsumptionService.getAllByRoomId(roomId)));
    }

    @GetMapping("/power-consumptions/{id}")
    public ResponseEntity<ApiResponse<PowerConsumptionDto>> getById(
            @PathVariable(name = "id") Long id) {
        
        return ResponseEntity.ok(ApiResponse.ok(powerConsumptionService.getById(id)));
    }

    @PostMapping("/power-consumptions")
    public ResponseEntity<ApiResponse<PowerConsumptionDto>> create(
            @RequestBody @Valid CreatePowerConsumptionDto dto) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(powerConsumptionService.create(dto)));
    }

    @PutMapping("/power-consumptions/{id}")
    public ResponseEntity<ApiResponse<PowerConsumptionDto>> update(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdatePowerConsumptionDto dto) {
        
        return ResponseEntity.ok(ApiResponse.ok(powerConsumptionService.update(id, dto)));
    }

    @DeleteMapping("/power-consumptions/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable(name = "id") Long id) {
        powerConsumptionService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, 
                "Deleted successfully"));
    }
}
