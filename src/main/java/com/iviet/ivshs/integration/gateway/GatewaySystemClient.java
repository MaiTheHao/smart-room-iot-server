package com.iviet.ivshs.integration.gateway;

import com.iviet.ivshs.dto.setup.SetupRequest;
import com.iviet.ivshs.dto.system.HealthCheckResponseDto;
import com.iviet.ivshs.integration.gateway.base.BaseGatewayClient;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

@Service
@RequiredArgsConstructor
public class GatewaySystemClient extends BaseGatewayClient {

    @Qualifier("GatewayTelemetryRestTemplate")
    private final RestTemplate restTemplate;

    public ResponseEntity<SetupRequest> fetchSetup(String ip) {
        String url = buildUri(ip, API_V2, "setup");
        return restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<SetupRequest>() {
        });
    }

    public ResponseEntity<HealthCheckResponseDto> fetchHealthCheck(String ip) {
        String url = buildUri(ip, API_V2, "health-check");
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<HealthCheckResponseDto>() {
                });
    }
}
