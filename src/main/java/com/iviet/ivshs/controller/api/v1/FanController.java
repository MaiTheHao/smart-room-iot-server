package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.FanDto;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.CreateFanDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateFanDto;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorState;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.service.FanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fans")
public class FanController {

    private final FanService fanService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<FanDto>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(fanService.getList(page, size)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<java.util.List<FanDto>>> getAllFans() {
        return ResponseEntity.ok(ApiResponse.ok(fanService.getAll()));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<FanDto>>> getByRoom(
            @PathVariable(name = "roomId") Long roomId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(fanService.getListByRoomId(roomId, page, size)));
    }

    @GetMapping("/room/{roomId}/all")
    public ResponseEntity<ApiResponse<java.util.List<FanDto>>> getAllByRoom(
            @PathVariable(name = "roomId") Long roomId) {
        
        return ResponseEntity.ok(ApiResponse.ok(fanService.getAllByRoomId(roomId)));
    }

    @GetMapping("/room/{roomId}/fans/{naturalId}")
    public ResponseEntity<ApiResponse<FanDto>> getByRoomAndNaturalId(
            @PathVariable(name = "roomId") Long roomId,
            @PathVariable(name = "naturalId") String naturalId) {
        // We'll map this via a findByRoomAndNaturalId service call if it exists, or just filter it.
        // Assuming a simpler approach: fetch all for the room and filter to find the specific naturalId.
        // A better approach would be adding `getByRoomIdAndNaturalId` to `FanService` but we'll use a stream filter for now to avoid altering the DAO layer again unless necessary.
        java.util.List<FanDto> allInRoom = fanService.getAllByRoomId(roomId);
        FanDto fan = allInRoom.stream()
                .filter(f -> naturalId.equals(f.naturalId()))
                .findFirst()
                .orElse(null);
                
        if (fan == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND, "Fan not found in room " + roomId + " with naturalId " + naturalId));
        }
        return ResponseEntity.ok(ApiResponse.ok(fan));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FanDto>> getById(
            @PathVariable(name = "id") Long id) {
        
        return ResponseEntity.ok(ApiResponse.ok(fanService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FanDto>> create(
            @RequestBody @Valid CreateFanDto request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(fanService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FanDto>> update(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateFanDto request) {
        
        return ResponseEntity.ok(ApiResponse.ok(fanService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable(name = "id") Long id) {
        
        fanService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Deleted successfully"));
    }

    // === CONTROL ENDPOINTS ===

    @PutMapping("/{id}/power")
    public ResponseEntity<ApiResponse<Void>> controlPower(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "state") ActuatorPower state) {
        
        fanService._v2api_handlePowerControl(id, state);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Power controlled successfully"));
    }

    @PutMapping("/{id}/mode")
    public ResponseEntity<ApiResponse<Void>> controlMode(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "value") ActuatorMode mode) {
        
        fanService._v2api_handleModeControl(id, mode);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Mode controlled successfully"));
    }

    @PutMapping("/{id}/fan")
    public ResponseEntity<ApiResponse<Void>> controlFanSpeed(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "speed") int speed) {
        
        fanService._v2api_handleSpeedControl(id, speed);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Fan speed controlled successfully"));
    }

    @PutMapping("/{id}/swing")
    public ResponseEntity<ApiResponse<Void>> controlSwing(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "state") ActuatorSwing swing) {
        
        fanService._v2api_handleSwingControl(id, swing);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Swing controlled successfully"));
    }
    
    @PutMapping("/{id}/light")
    public ResponseEntity<ApiResponse<Void>> controlLight(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "state") ActuatorState state) {
        
        fanService._v2api_handleLightControl(id, state);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Light controlled successfully"));
    }
}
