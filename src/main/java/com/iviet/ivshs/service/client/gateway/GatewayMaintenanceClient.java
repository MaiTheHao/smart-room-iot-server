package com.iviet.ivshs.service.client.gateway;

import com.iviet.ivshs.dto.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

@Service
@RequiredArgsConstructor
public class GatewayMaintenanceClient extends GatewayBaseClient {

    @Qualifier("GatewayApiClient")
    private final RestTemplate restTemplate;

    public ResponseEntity<ApiResponse<String>> resetAcEnergy(String ip, String naturalId) {
        return executeReset(ip, "air-conditions", naturalId);
    }

    public ResponseEntity<ApiResponse<String>> resetFanEnergy(String ip, String naturalId) {
        return executeReset(ip, "fans", naturalId);
    }

    public ResponseEntity<ApiResponse<String>> resetLightEnergy(String ip, String naturalId) {
        return executeReset(ip, "lights", naturalId);
    }

    public ResponseEntity<ApiResponse<String>> resetRoomEnergy(String ip, String naturalId) {
        return executeReset(ip, "power-consumptions", naturalId);
    }

    // --- Private Helper Methods ---
    private ResponseEntity<ApiResponse<String>> executeReset(String ip, String resource, String naturalId) {
        String url = buildUri(ip, API_V1, String.format("%s/%s/reset", resource, naturalId));
        return restTemplate.exchange(url, HttpMethod.POST, HttpEntity.EMPTY, new ParameterizedTypeReference<ApiResponse<String>>() {});
    }
}