package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.LightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lights")
public class LightController {

    private final LightService lightService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<LightDto>>> getLights(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(lightService.getList(page, size)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<java.util.List<LightDto>>> getAllLights() {
        return ResponseEntity.ok(ApiResponse.ok(lightService.getAll()));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<LightDto>>> getLightsByRoom(
            @PathVariable(name = "roomId") Long roomId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(lightService.getListByRoomId(roomId, page, size)));
    }

    @GetMapping("/room/{roomId}/all")
    public ResponseEntity<ApiResponse<java.util.List<LightDto>>> getAllLightsByRoom(
            @PathVariable(name = "roomId") Long roomId) {
        
        return ResponseEntity.ok(ApiResponse.ok(lightService.getAllByRoomId(roomId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LightDto>> getLightById(
            @PathVariable(name = "id") Long id) {
        
        return ResponseEntity.ok(ApiResponse.ok(lightService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LightDto>> createLight(
            @RequestBody @Valid CreateLightDto request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(lightService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LightDto>> updateLight(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateLightDto request) {
        
        return ResponseEntity.ok(ApiResponse.ok(lightService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLight(
            @PathVariable(name = "id") Long id) {
        
        lightService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Deleted successfully"));
    }
    
    @PutMapping("/{id}/toggle-state")
    public ResponseEntity<ApiResponse<ControlDeviceResponse>> handleToggleStateControl(
            @PathVariable(name = "id") Long id) {
        lightService.handleToggleStateControl(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Toggled successfully"));
    }

    @PutMapping("/{id}/level/{newLevel}")
    public ResponseEntity<ApiResponse<ControlDeviceResponse>> setLevel(
            @PathVariable(name = "id") Long id,
            @PathVariable(name = "newLevel") int newLevel) {
        
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Level set successfully"));
    }
}