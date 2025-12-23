package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.FloorServiceV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/floors")
public class FloorControllerV1 {

    private final FloorServiceV1 floorService;

    @GetMapping
    public ResponseEntity<ApiResponseV1<PaginatedResponseV1<FloorDtoV1>>> getFloors(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(floorService.getList(page, size)));
    }

    @GetMapping("/{floorId}")
    public ResponseEntity<ApiResponseV1<FloorDtoV1>> getFloorById(
            @PathVariable(name = "floorId") Long floorId) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(floorService.getById(floorId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseV1<FloorDtoV1>> createFloor(
            @RequestBody @Valid CreateFloorDtoV1 request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseV1.created(floorService.create(request)));
    }

    @PutMapping("/{floorId}")
    public ResponseEntity<ApiResponseV1<FloorDtoV1>> updateFloor(
            @PathVariable(name = "floorId") Long floorId,
            @RequestBody @Valid UpdateFloorDtoV1 request) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(floorService.update(floorId, request)));
    }

    @DeleteMapping("/{floorId}")
    public ResponseEntity<ApiResponseV1<Void>> deleteFloor(
            @PathVariable(name = "floorId") Long floorId) {
        
        floorService.delete(floorId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponseV1.success(HttpStatus.NO_CONTENT, null, "Deleted successfully"));
    }
}