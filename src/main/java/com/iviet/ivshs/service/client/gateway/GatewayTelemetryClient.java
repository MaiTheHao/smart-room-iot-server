package com.iviet.ivshs.service.client.gateway;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.EnergyMetricDto;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayTelemetryClient extends GatewayBaseClient {

    @Qualifier("GatewayApiClient")
    private final RestTemplate restTemplate;

    public ResponseEntity<ApiResponse<EnergyMetricDto>> fetchLightEnergyMetric(String ip, String naturalId) {
        return executeGetTelemetry(ip, "lights", naturalId);
    }

    public ResponseEntity<ApiResponse<EnergyMetricDto>> fetchFanEnergyMetric(String ip, String naturalId) {
        return executeGetTelemetry(ip, "fans", naturalId);
    }

    public ResponseEntity<ApiResponse<EnergyMetricDto>> fetchAcEnergyMetric(String ip, String naturalId) {
        return executeGetTelemetry(ip, "air-conditions", naturalId);
    }

    public ResponseEntity<ApiResponse<EnergyMetricDto>> fetchRoomEnergyMetric(String ip, String naturalId) {
        return executeGetTelemetry(ip, "power-consumptions", naturalId);
    }

    public ResponseEntity<TelemetryResponseDto> fetchGlobalTelemetry(String ip) {
        String url = buildUri(ip, API_V1, "telemetry");
        return restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                null, 
                new ParameterizedTypeReference<TelemetryResponseDto>() {}
        );
    }

    private ResponseEntity<ApiResponse<EnergyMetricDto>> executeGetTelemetry(String ip, String resource, String naturalId) {
        String url = buildUri(ip, API_V1, String.format("%s/%s/telemetry", resource, naturalId));
        return restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                null, 
                new ParameterizedTypeReference<ApiResponse<EnergyMetricDto>>() {}
        );
    }
}