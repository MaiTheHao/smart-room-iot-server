package com.iviet.ivshs.controller.api.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.SensorMetadataDto;
import com.iviet.ivshs.service.SensorMetadataService;
import com.iviet.ivshs.service.PermissionService;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class SensorMetadataController {

    private final SensorMetadataService sensorMetadataService;
    private final PermissionService permissionService;

    @GetMapping("/rooms/{roomId}/sensors")
    public ResponseEntity<ApiResponse<PaginatedResponse<SensorMetadataDto>>> getAllByRoomId(
        @PathVariable(name = "roomId") Long roomId,
        @RequestParam(name = "category", required = false) DeviceCategory category,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        permissionService.requireAccessRoom(roomId);
        PaginatedResponse<SensorMetadataDto> sensors = sensorMetadataService.getAllByRoomId(roomId, category, page, size);
        return ResponseEntity.ok(ApiResponse.ok(sensors));
    }

    @GetMapping("/rooms/{roomId}/sensors/count")
    public ResponseEntity<ApiResponse<Long>> getCountByRoomId(
        @PathVariable(name = "roomId") Long roomId
    ) {
        permissionService.requireAccessRoom(roomId);
        Long count = sensorMetadataService.getCountByRoomId(roomId);
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    @GetMapping("/sensors")
    public ResponseEntity<ApiResponse<PaginatedResponse<SensorMetadataDto>>> getAll(
        @RequestParam(name = "category", required = false) DeviceCategory category,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        PaginatedResponse<SensorMetadataDto> sensors = sensorMetadataService.getAll(category, page, size);
        return ResponseEntity.ok(ApiResponse.ok(sensors));
    }

    // Tương thích ngược
    @GetMapping("/sensors/all")
    public ResponseEntity<ApiResponse<PaginatedResponse<SensorMetadataDto>>> getAllLegacy(
        @RequestParam(name = "category", required = false) DeviceCategory category,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return getAll(category, page, size);
    }

    @GetMapping("/sensors/{sensorId}")
    public ResponseEntity<ApiResponse<SensorMetadataDto>> getById(
        @PathVariable(name = "sensorId") Long sensorId,
        @RequestParam(name = "category") DeviceCategory category
    ) {
        SensorMetadataDto sensor = sensorMetadataService.getSensorById(sensorId, category);
        permissionService.requireAccessRoom(sensor.roomId());
        return ResponseEntity.ok(ApiResponse.ok(sensor));
    }

    @GetMapping("/sensors/natural/{naturalId}")
    public ResponseEntity<ApiResponse<SensorMetadataDto>> getByNaturalId(
        @PathVariable(name = "naturalId") String naturalId,
        @RequestParam(name = "category") DeviceCategory category
    ) {
        SensorMetadataDto sensor = sensorMetadataService.getSensorByNaturalId(naturalId, category);
        permissionService.requireAccessRoom(sensor.roomId());
        return ResponseEntity.ok(ApiResponse.ok(sensor));
    }
}
