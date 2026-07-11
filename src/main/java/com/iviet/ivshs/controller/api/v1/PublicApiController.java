package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.ServerTimeResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/v1/public")
public class PublicApiController {

    @GetMapping("/time")
    public ResponseEntity<ApiResponse<ServerTimeResponseDto>> getServerTime() {
        return ResponseEntity.ok(ApiResponse.ok(new ServerTimeResponseDto(Instant.now())));
    }
}
