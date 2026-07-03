package com.iviet.ivshs.integration.gateway.impl.esp32;

import com.iviet.ivshs.dto.setup.SetupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class Esp32SystemClient extends Esp32BaseClient {

    @Qualifier("GatewayTelemetryRestTemplate")
    private final RestTemplate restTemplate;

    public ResponseEntity<SetupRequest> fetchSetup(String ip) {
        String url = buildEsp32Uri(ip, "setup");
        return restTemplate.exchange(url, HttpMethod.GET, null,
            new ParameterizedTypeReference<SetupRequest>() {});
    }
}
