package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.TemperatureServiceV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TemperatureControllerV1 {

    private final TemperatureServiceV1 temperatureService;

    @GetMapping("/rooms/{roomId}/temperatures")
    public ResponseEntity<ApiResponse<PaginatedResponse<TemperatureDto>>> getListByRoom(
            @PathVariable(name = "roomId") Long roomId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(temperatureService.getListByRoom(roomId, page, size)));
    }

    @GetMapping("/temperatures/{id}")
    public ResponseEntity<ApiResponse<TemperatureDto>> getById(
            @PathVariable(name = "id") Long id) {
        
        return ResponseEntity.ok(ApiResponse.ok(temperatureService.getById(id)));
    }

    @PostMapping("/temperatures")
    public ResponseEntity<ApiResponse<TemperatureDto>> create(
            @RequestBody @Valid CreateTemperatureDto dto) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(temperatureService.create(dto)));
    }

    @PutMapping("/temperatures/{id}")
    public ResponseEntity<ApiResponse<TemperatureDto>> update(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateTemperatureDto dto) {
        
        return ResponseEntity.ok(ApiResponse.ok(temperatureService.update(id, dto)));
    }

    @DeleteMapping("/temperatures/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable(name = "id") Long id) {
        temperatureService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Deleted successfully"));
    }
}