package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.TemperatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TemperatureController {

    private final TemperatureService temperatureService;

    @GetMapping("/temperatures")
    public ResponseEntity<ApiResponse<PaginatedResponse<TemperatureDto>>> getList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(temperatureService.getList(page, size)));
    }

    @GetMapping("/temperatures/all")
    public ResponseEntity<ApiResponse<java.util.List<TemperatureDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(temperatureService.getAll()));
    }

    @GetMapping("/rooms/{roomId}/temperatures")
    public ResponseEntity<ApiResponse<PaginatedResponse<TemperatureDto>>> getListByRoomId(
            @PathVariable(name = "roomId") Long roomId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(temperatureService.getListByRoomId(roomId, page, size)));
    }

    @GetMapping("/rooms/{roomId}/temperatures/all")
    public ResponseEntity<ApiResponse<java.util.List<TemperatureDto>>> getAllByRoomId(
            @PathVariable(name = "roomId") Long roomId) {
        
        return ResponseEntity.ok(ApiResponse.ok(temperatureService.getAllByRoomId(roomId)));
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