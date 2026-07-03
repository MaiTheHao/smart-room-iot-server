package com.iviet.ivshs.integration.gateway.impl.esp32;

import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.integration.gateway.GatewayCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Esp32FanControlClient extends Esp32BaseClient {

    @Qualifier("GatewayControlRestTemplate")
    private final RestTemplate restTemplate;

    public ResponseEntity<ApiResponse<String>> controlFan(String ip, GatewayCommand command) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("naturalId", command.naturalId());
        body.put("category", "FAN");
        Object power = command.param("power");
        Object speed = command.param("speed");
        if (power != null) body.put("power", Boolean.TRUE.equals(power) ? "ON" : "OFF");
        if (speed != null) body.put("speed", speed);
        if (body.size() == 2) return null;

        String url = buildEsp32Uri(ip, "control");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body);
        return restTemplate.exchange(url, HttpMethod.POST, request,
            new ParameterizedTypeReference<ApiResponse<String>>() {});
    }
}
