package com.iviet.ivshs.integration.gateway.impl.esp32;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.integration.gateway.GatewayCommand;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;

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
public class Esp32LightControlClient extends Esp32BaseClient {

    @Qualifier("GatewayControlRestTemplate")
    private final RestTemplate restTemplate;

    public ResponseEntity<ApiResponse<String>> controlLight(String ip, GatewayCommand command) {
        Object power = command.param("power");
        if (power == null) return null;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("naturalId", command.naturalId());
        body.put("category", DeviceCategory.LIGHT);
        body.put("power", ActuatorPower.ON.equals(power) ? "ON" : "OFF");

        String url = buildEsp32Uri(ip, "control");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body);
        return restTemplate.exchange(url, HttpMethod.POST, request,
            new ParameterizedTypeReference<ApiResponse<String>>() {});
    }
}
