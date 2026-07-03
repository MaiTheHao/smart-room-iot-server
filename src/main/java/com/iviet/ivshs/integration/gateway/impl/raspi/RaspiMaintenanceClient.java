package com.iviet.ivshs.integration.gateway.impl.raspi;

import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.integration.gateway.base.BaseGatewayClient;

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
public class RaspiMaintenanceClient extends BaseGatewayClient {

    @Qualifier("GatewayTelemetryRestTemplate")
    private final RestTemplate restTemplate;

    public ResponseEntity<ApiResponse<String>> resetAcEnergy(String ip, String naturalId) {
        return executeReset(ip, String.format("ac/%s/resetEnergy", naturalId));
    }

    public ResponseEntity<ApiResponse<String>> resetFanEnergy(String ip, String naturalId) {
        return executeReset(ip, String.format("fan/%s/resetEnergy", naturalId));
    }

    public ResponseEntity<ApiResponse<String>> resetLightEnergy(String ip, String naturalId) {
        return executeReset(ip, String.format("light/%s/resetEnergy", naturalId));
    }

    public ResponseEntity<ApiResponse<String>> resetRoomEnergy(String ip, String naturalId) {
        return executeReset(ip, String.format("power-consumption/resetEnergy/%s", naturalId));
    }

    public ResponseEntity<ApiResponse<String>> resetByPath(String ip, String pathSegment, String naturalId) {
        String endpoint;
        if ("power-consumption".equals(pathSegment)) {
            endpoint = String.format("power-consumption/resetEnergy/%s", naturalId);
        } else {
            endpoint = String.format("%s/%s/resetEnergy", pathSegment, naturalId);
        }
        return executeReset(ip, endpoint);
    }

    private ResponseEntity<ApiResponse<String>> executeReset(String ip, String endpoint) {
        String url = buildUri(ip, API_V2, endpoint);
        return restTemplate.exchange(url, HttpMethod.PUT, HttpEntity.EMPTY, new ParameterizedTypeReference<ApiResponse<String>>() {});
    }
}
