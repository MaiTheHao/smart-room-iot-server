package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.AirConditionDto;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.CreateAirConditionDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateAirConditionDto;
import com.iviet.ivshs.enumeration.AcMode;
import com.iviet.ivshs.enumeration.AcPower;
import com.iviet.ivshs.enumeration.AcSwing;
import com.iviet.ivshs.service.AirConditionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/air-conditions")
public class AirConditionController {

    private final AirConditionService airConditionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<AirConditionDto>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(airConditionService.getList(page, size)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<java.util.List<AirConditionDto>>> getAllAirConditions() {
        return ResponseEntity.ok(ApiResponse.ok(airConditionService.getAll()));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<AirConditionDto>>> getByRoom(
            @PathVariable(name = "roomId") Long roomId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(airConditionService.getListByRoomId(roomId, page, size)));
    }

    @GetMapping("/room/{roomId}/all")
    public ResponseEntity<ApiResponse<java.util.List<AirConditionDto>>> getAllByRoom(
            @PathVariable(name = "roomId") Long roomId) {
        
        return ResponseEntity.ok(ApiResponse.ok(airConditionService.getAllByRoomId(roomId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AirConditionDto>> getById(
            @PathVariable(name = "id") Long id) {
        
        return ResponseEntity.ok(ApiResponse.ok(airConditionService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AirConditionDto>> create(
            @RequestBody @Valid CreateAirConditionDto request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(airConditionService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AirConditionDto>> update(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateAirConditionDto request) {
        
        return ResponseEntity.ok(ApiResponse.ok(airConditionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable(name = "id") Long id) {
        
        airConditionService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Deleted successfully"));
    }

    // === CONTROL ENDPOINTS ===

    @PostMapping("/{id}/power")
    public ResponseEntity<ApiResponse<Void>> controlPower(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "state") AcPower state) {
        
        airConditionService.controlPower(id, state);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Power controlled successfully"));
    }

    @PostMapping("/{id}/temperature")
    public ResponseEntity<ApiResponse<Void>> controlTemperature(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "value") int temperature) {
        
        airConditionService.controlTemperature(id, temperature);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Temperature controlled successfully"));
    }

    @PostMapping("/{id}/mode")
    public ResponseEntity<ApiResponse<Void>> controlMode(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "value") AcMode mode) {
        
        airConditionService.controlMode(id, mode);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Mode controlled successfully"));
    }

    @PostMapping("/{id}/fan")
    public ResponseEntity<ApiResponse<Void>> controlFanSpeed(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "speed") int speed) {
        
        airConditionService.controlFanSpeed(id, speed);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Fan speed controlled successfully"));
    }

    @PostMapping("/{id}/swing")
    public ResponseEntity<ApiResponse<Void>> controlSwing(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "state") AcSwing swing) {
        
        airConditionService.controlSwing(id, swing);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Swing controlled successfully"));
    }
}