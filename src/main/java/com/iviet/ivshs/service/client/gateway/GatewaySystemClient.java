package com.iviet.ivshs.service.client.gateway;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.HealthCheckResponseDto;
import com.iviet.ivshs.dto.SetupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

@Service
@RequiredArgsConstructor
public class GatewaySystemClient extends GatewayBaseClient {

    @Qualifier("GatewayApiClient")
    private final RestTemplate restTemplate;

    public ResponseEntity<SetupRequest> fetchSetup(String ip) {
        String url = buildUri(ip, API_V1, "setup");
        return restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<SetupRequest>() {});
    }

    public ResponseEntity<HealthCheckResponseDto> fetchHealthCheck(String ip) {
        String url = buildUri(ip, API_V1, "health-check");
        return restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<HealthCheckResponseDto>() {});
    }
}
