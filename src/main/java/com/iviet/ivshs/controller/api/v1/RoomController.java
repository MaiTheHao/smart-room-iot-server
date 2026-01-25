package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/rooms/all")
    public ResponseEntity<ApiResponse<List<RoomDto>>> getAllRooms() {
        return ResponseEntity.ok(ApiResponse.ok(roomService.getAll()));
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<PaginatedResponse<RoomDto>>> getRooms(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(roomService.getList(page, size)));
    }

    @GetMapping("/floors/{floorId}/rooms")
    public ResponseEntity<ApiResponse<PaginatedResponse<RoomDto>>> getRoomsByFloor(
            @PathVariable(name = "floorId") Long floorId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(roomService.getListByFloor(floorId, page, size)));
    }

    @GetMapping("/floors/{floorId}/rooms/all")
    public ResponseEntity<ApiResponse<List<RoomDto>>> getAllRoomsByFloor(
            @PathVariable(name = "floorId") Long floorId) {
        
        return ResponseEntity.ok(ApiResponse.ok(roomService.getAllByFloor(floorId)));
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<RoomDto>> getRoomById(@PathVariable(name = "roomId") Long roomId) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.getById(roomId)));
    }

    @PostMapping("/floors/{floorId}/rooms")
    public ResponseEntity<ApiResponse<RoomDto>> createRoom(
            @PathVariable(name = "floorId") Long floorId,
            @RequestBody @Valid CreateRoomDto request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(roomService.create(floorId, request)));
    }

    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<RoomDto>> updateRoom(
            @PathVariable(name = "roomId") Long roomId,
            @RequestBody @Valid UpdateRoomDto request) {
        
        return ResponseEntity.ok(ApiResponse.ok(roomService.update(roomId, request)));
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(
            @PathVariable(name = "roomId") Long roomId) {
        
        roomService.delete(roomId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Deleted successfully"));
    }
}