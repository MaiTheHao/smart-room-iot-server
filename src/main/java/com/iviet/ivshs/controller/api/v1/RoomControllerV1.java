package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.RoomServiceV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class RoomControllerV1 {

    private final RoomServiceV1 roomService;

    @GetMapping("/floors/{floorId}/rooms")
    public ResponseEntity<ApiResponseV1<PaginatedResponseV1<RoomDtoV1>>> getRoomsByFloor(
            @PathVariable(name = "floorId") Long floorId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(roomService.getListByFloor(floorId, page, size)));
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponseV1<RoomDtoV1>> getRoomById(
            @PathVariable(name = "roomId") Long roomId) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(roomService.getById(roomId)));
    }

    @PostMapping("/floors/{floorId}/rooms")
    public ResponseEntity<ApiResponseV1<RoomDtoV1>> createRoom(
            @PathVariable(name = "floorId") Long floorId,
            @RequestBody @Valid CreateRoomDtoV1 request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseV1.created(roomService.create(floorId, request)));
    }

    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponseV1<RoomDtoV1>> updateRoom(
            @PathVariable(name = "roomId") Long roomId,
            @RequestBody @Valid UpdateRoomDtoV1 request) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(roomService.update(roomId, request)));
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponseV1<Void>> deleteRoom(
            @PathVariable(name = "roomId") Long roomId) {
        
        roomService.delete(roomId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponseV1.success(HttpStatus.NO_CONTENT, null, "Deleted successfully"));
    }
}