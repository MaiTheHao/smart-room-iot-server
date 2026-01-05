package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.LightServiceV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lights")
public class LightControllerV1 {

    private final LightServiceV1 lightService;

    @GetMapping
    public ResponseEntity<ApiResponseV1<PaginatedResponseV1<LightDtoV1>>> getLights(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(lightService.getList(page, size)));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponseV1<PaginatedResponseV1<LightDtoV1>>> getLightsByRoom(
            @PathVariable(name = "roomId") Long roomId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(lightService.getListByRoomId(roomId, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseV1<LightDtoV1>> getLightById(
            @PathVariable(name = "id") Long id) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(lightService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseV1<LightDtoV1>> createLight(
            @RequestBody @Valid CreateLightDtoV1 request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseV1.created(lightService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseV1<LightDtoV1>> updateLight(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateLightDtoV1 request) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(lightService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseV1<Void>> deleteLight(
            @PathVariable(name = "id") Long id) {
        
        lightService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponseV1.success(HttpStatus.NO_CONTENT, null, "Deleted successfully"));
    }
    
    @PutMapping("/{id}/toggle-state")
    public ResponseEntity<ApiResponseV1<ControlDeviceResponseV1>> toggleState(
            @PathVariable(name = "id") Long id) {
        lightService.toggleState(id);
        return ResponseEntity.ok(ApiResponseV1.success(HttpStatus.ACCEPTED, null, "Toggled successfully"));
    }

    @PutMapping("/{id}/level/{newLevel}")
    public ResponseEntity<ApiResponseV1<ControlDeviceResponseV1>> setLevel(
            @PathVariable(name = "id") Long id,
            @PathVariable(name = "newLevel") int newLevel) {
        
        return ResponseEntity.ok(ApiResponseV1.success(HttpStatus.ACCEPTED, null, "Level set successfully"));
    }
}