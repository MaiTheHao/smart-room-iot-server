package com.iviet.ivshs.integration.gateway.impl.raspi;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.DeviceControlPayload;
import com.iviet.ivshs.integration.gateway.base.BaseGatewayClient;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public abstract class RaspiDeviceControlClient extends BaseGatewayClient {

    protected final RestTemplate restTemplate;

    protected RaspiDeviceControlClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected String buildUrl(String ip, String deviceType, String naturalId, String action) {
        return buildUri(ip, API_V2, String.format("%s/%s/%s", deviceType, naturalId, action));
    }

    protected ResponseEntity<ApiResponse<String>> executePut(String url, Object data) {
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(Map.of("data", data));
        return restTemplate.exchange(url, HttpMethod.PUT, requestEntity, new ParameterizedTypeReference<ApiResponse<String>>() {});
    }

    protected ResponseEntity<ApiResponse<String>> executePut(String url, DeviceControlPayload payload) {
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload.toMap());
        return restTemplate.exchange(url, HttpMethod.PUT, requestEntity, new ParameterizedTypeReference<ApiResponse<String>>() {});
    }
}
