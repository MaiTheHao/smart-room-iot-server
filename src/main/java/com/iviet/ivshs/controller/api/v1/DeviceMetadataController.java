package com.iviet.ivshs.controller.api.v1;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.service.DeviceMetadataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DeviceMetadataController {

    private final DeviceMetadataService deviceMetadataService;


    @GetMapping("/rooms/{roomId}/devices")
    public ResponseEntity<ApiResponse<List<Object>>> getAllByRoomId(
            @PathVariable(name = "roomId") Long roomId,
            @RequestParam(name = "category", required = false) DeviceCategory category) {
        List<Object> devices = deviceMetadataService.getAllByRoomId(roomId, category);
        return ResponseEntity.ok(ApiResponse.ok(devices));
    }

    @GetMapping("/rooms/{roomId}/devices/count")
    public ResponseEntity<ApiResponse<Long>> getCountByRoomId(@PathVariable(name = "roomId") Long roomId) {
        Long count = deviceMetadataService.getCountByRoomId(roomId);
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

}
