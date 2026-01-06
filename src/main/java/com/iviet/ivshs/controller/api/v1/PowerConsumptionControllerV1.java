package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.PowerConsumptionServiceV1;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class PowerConsumptionControllerV1 {

    @Autowired
    private PowerConsumptionServiceV1 powerConsumptionService;

    @GetMapping("/rooms/{roomId}/power-consumptions")
    public ResponseEntity<ApiResponse<PaginatedResponse<PowerConsumptionDto>>> 
            getListByRoom(
                @PathVariable(name = "roomId") Long roomId,
                @RequestParam(name = "page", defaultValue = "0") int page,
                @RequestParam(name = "size", defaultValue = "20") int size) {
        PaginatedResponse<PowerConsumptionDto> paginated = 
            powerConsumptionService.getListByRoom(roomId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(paginated));
    }

    @GetMapping("/power-consumptions/{id}")
    public ResponseEntity<ApiResponse<PowerConsumptionDto>> getById(
            @PathVariable(name = "id") Long id) {
        PowerConsumptionDto dto = powerConsumptionService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping("/power-consumptions")
    public ResponseEntity<ApiResponse<PowerConsumptionDto>> create(
            @RequestBody @Valid CreatePowerConsumptionDto dto) {
        PowerConsumptionDto created = powerConsumptionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(created));
    }

    @PutMapping("/power-consumptions/{id}")
    public ResponseEntity<ApiResponse<PowerConsumptionDto>> update(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdatePowerConsumptionDto dto) {
        PowerConsumptionDto updated = powerConsumptionService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.ok(updated));
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
