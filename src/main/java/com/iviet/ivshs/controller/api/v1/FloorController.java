package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.FloorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/floors")
public class FloorController {

    private final FloorService floorService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<FloorDto>>> getFloors(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(floorService.getList(page, size)));
    }

    @GetMapping("/{floorId}")
    public ResponseEntity<ApiResponse<FloorDto>> getFloorById(
            @PathVariable(name = "floorId") Long floorId) {
        
        return ResponseEntity.ok(ApiResponse.ok(floorService.getById(floorId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FloorDto>> createFloor(
            @RequestBody @Valid CreateFloorDto request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(floorService.create(request)));
    }

    @PutMapping("/{floorId}")
    public ResponseEntity<ApiResponse<FloorDto>> updateFloor(
            @PathVariable(name = "floorId") Long floorId,
            @RequestBody @Valid UpdateFloorDto request) {
        
        return ResponseEntity.ok(ApiResponse.ok(floorService.update(floorId, request)));
    }

    @DeleteMapping("/{floorId}")
    public ResponseEntity<ApiResponse<Void>> deleteFloor(
            @PathVariable(name = "floorId") Long floorId) {
        
        floorService.delete(floorId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Deleted successfully"));
    }
}