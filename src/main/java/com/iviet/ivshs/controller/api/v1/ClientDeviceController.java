package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.clientdevice.RegisterClientDeviceDto;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.service.clientdevice.ClientDeviceService;
import com.iviet.ivshs.service.notification.FcmDispatchService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/client-devices")
@Validated
@RequiredArgsConstructor
public class ClientDeviceController {

    private final ClientDeviceService clientDeviceService;
    private final FcmDispatchService fcmDispatchService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerDevice(@RequestBody
    @Valid
    RegisterClientDeviceDto request) {
        clientDeviceService.registerDevice(request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // TODO: Đây là API TEST
    @PostMapping("/test-fcm")
    public ResponseEntity<ApiResponse<Void>> testFcm(@RequestBody
    @Valid
    TestFcmRequest request) {
        if (request.getTokens() != null && !request.getTokens()
                .isEmpty()) {
            fcmDispatchService.sendToMultipleDevices(request.getTokens(), request.getTitle(), request.getBody(), request.getData());
        } else if (request.getToken() != null && !request.getToken()
                .trim()
                .isEmpty()) {
            fcmDispatchService.sendToSingleDevice(request.getToken(), request.getTitle(), request.getBody(), request.getData());
        }
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Data
    public static class TestFcmRequest {
        private String token;
        private List<String> tokens;
        private String title;
        private String body;
        private Map<String, String> data;
    }
}

