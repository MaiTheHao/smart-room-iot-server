package com.iviet.ivshs.integration.gateway.impl.esp32;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class Esp32TelemetryClient extends Esp32BaseClient {

    @Qualifier("GatewayTelemetryRestTemplate")
    private final RestTemplate restTemplate;

    public ResponseEntity<TelemetryResponseDto> fetchGlobalTelemetry(String ip) {
        String url = buildEsp32Uri(ip, "telemetry");
        return restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<TelemetryResponseDto>() {}
        );
    }

    public ResponseEntity<ApiResponse<JsonNode>> fetchTemperature(String ip, String naturalId) {
        String url = buildEsp32Uri(ip, "temperature");
        record RequestBody(String naturalId) {}
        HttpEntity<RequestBody> entity = new HttpEntity<>(new RequestBody(naturalId));
        return restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<ApiResponse<JsonNode>>() {}
        );
    }
}
