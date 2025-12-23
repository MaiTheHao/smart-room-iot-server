package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.DeviceControlServiceV1;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/device-controls")
public class DeviceControlControllerV1 {

    @Autowired
    private DeviceControlServiceV1 deviceControlService;

    // --- CRUD DEVICE CONTROL ---

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseV1<DeviceControlDtoV1>> getById(
            @PathVariable(name = "id") Long id) {
        DeviceControlDtoV1 dto = deviceControlService.getById(id);
        return ResponseEntity.ok(ApiResponseV1.ok(dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponseV1<DeviceControlDtoV1>> create(
            @RequestBody @Valid CreateDeviceControlDtoV1 dto) {
        DeviceControlDtoV1 created = deviceControlService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponseV1.created(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseV1<DeviceControlDtoV1>> update(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateDeviceControlDtoV1 dto) {
        DeviceControlDtoV1 updated = deviceControlService.update(id, dto);
        return ResponseEntity.ok(ApiResponseV1.ok(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseV1<Void>> delete(
            @PathVariable(name = "id") Long id) {
        deviceControlService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(ApiResponseV1.success(HttpStatus.NO_CONTENT, null, 
                "Deleted successfully"));
    }

    // --- LIST BY RELATIONSHIP ---

    @GetMapping("client/{clientId}")
    public ResponseEntity<ApiResponseV1<PaginatedResponseV1<DeviceControlDtoV1>>> 
            getListByClient(
                @PathVariable(name = "clientId") Long clientId,
                @RequestParam(name = "page", defaultValue = "0") int page,
                @RequestParam(name = "size", defaultValue = "10") int size) {
        PaginatedResponseV1<DeviceControlDtoV1> paginated = 
            deviceControlService.getListByClientId(clientId, page, size);
        return ResponseEntity.ok(ApiResponseV1.ok(paginated));
    }

    @GetMapping("room/{roomId}")
    public ResponseEntity<ApiResponseV1<PaginatedResponseV1<DeviceControlDtoV1>>> 
            getListByRoom(
                @PathVariable(name = "roomId") Long roomId,
                @RequestParam(name = "page", defaultValue = "0") int page,
                @RequestParam(name = "size", defaultValue = "10") int size) {
        PaginatedResponseV1<DeviceControlDtoV1> paginated = 
            deviceControlService.getListByRoomId(roomId, page, size);
        return ResponseEntity.ok(ApiResponseV1.ok(paginated));
    }
}
