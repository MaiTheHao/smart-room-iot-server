package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.DeviceControlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/device-controls")
public class DeviceControlController {

    @Autowired
    private DeviceControlService deviceControlService;

    // --- CRUD DEVICE CONTROL ---

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceControlDto>> getById(
            @PathVariable(name = "id") Long id) {
        DeviceControlDto dto = deviceControlService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeviceControlDto>> create(
            @RequestBody @Valid CreateDeviceControlDto dto) {
        DeviceControlDto created = deviceControlService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceControlDto>> update(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateDeviceControlDto dto) {
        DeviceControlDto updated = deviceControlService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable(name = "id") Long id) {
        deviceControlService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, 
                "Deleted successfully"));
    }

    // --- LIST BY RELATIONSHIP ---

    @GetMapping("client/{clientId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<DeviceControlDto>>> 
            getListByClient(
                @PathVariable(name = "clientId") Long clientId,
                @RequestParam(name = "page", defaultValue = "0") int page,
                @RequestParam(name = "size", defaultValue = "10") int size) {
        PaginatedResponse<DeviceControlDto> paginated = 
            deviceControlService.getListByClientId(clientId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(paginated));
    }

    @GetMapping("room/{roomId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<DeviceControlDto>>> 
            getListByRoom(
                @PathVariable(name = "roomId") Long roomId,
                @RequestParam(name = "page", defaultValue = "0") int page,
                @RequestParam(name = "size", defaultValue = "10") int size) {
        PaginatedResponse<DeviceControlDto> paginated = 
            deviceControlService.getListByRoomId(roomId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(paginated));
    }
}
