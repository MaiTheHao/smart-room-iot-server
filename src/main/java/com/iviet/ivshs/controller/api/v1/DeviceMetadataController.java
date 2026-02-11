package com.iviet.ivshs.controller.api.v1;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.DeviceMetadataDto;
import com.iviet.ivshs.service.DeviceMetadataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DeviceMetadataController {

    private final DeviceMetadataService deviceMetadataService;

    @GetMapping("/devices/all")
    public ResponseEntity<ApiResponse<List<DeviceMetadataDto>>> getAll() {
        List<DeviceMetadataDto> devices = deviceMetadataService.getAll();
        return ResponseEntity.ok(ApiResponse.ok(devices));
    }

    @GetMapping("/rooms/{roomId}/devices")
    public ResponseEntity<ApiResponse<List<DeviceMetadataDto>>> getAllByRoomId(@PathVariable(name = "roomId") Long roomId) {
        List<DeviceMetadataDto> devices = deviceMetadataService.getAllByRoomId(roomId);
        return ResponseEntity.ok(ApiResponse.ok(devices));
    }
}
