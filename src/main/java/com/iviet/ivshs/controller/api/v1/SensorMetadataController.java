package com.iviet.ivshs.controller.api.v1;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.SensorMetadataDto;
import com.iviet.ivshs.service.control.SensorMetadataService;
import com.iviet.ivshs.service.permission.PermissionService;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class SensorMetadataController {

    private final SensorMetadataService sensorMetadataService;
    private final PermissionService permissionService;

    @GetMapping("/rooms/{roomId}/sensors")
    public ResponseEntity<ApiResponse<List<SensorMetadataDto>>> getAllByRoomId(
        @PathVariable(name = "roomId") Long roomId,
        @RequestParam(name = "category", required = false) DeviceCategory category
    ) {
        permissionService.requireAccessRoom(roomId);
        List<SensorMetadataDto> sensors = sensorMetadataService.getAllByRoomId(roomId, category);
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

    @GetMapping("/sensors/all")
    public ResponseEntity<ApiResponse<List<SensorMetadataDto>>> getAll(
        @RequestParam(name = "category", required = false) DeviceCategory category
    ) {
        List<SensorMetadataDto> sensors = sensorMetadataService.getAll(category);
        return ResponseEntity.ok(ApiResponse.ok(sensors));
    }
}
