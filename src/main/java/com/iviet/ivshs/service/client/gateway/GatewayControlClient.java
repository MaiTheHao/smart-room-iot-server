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

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GatewayControlClient extends GatewayBaseClient {

    @Qualifier("GatewayControlRestTemplate")
    private final RestTemplate restTemplate;

    // --- V1 Control ---

    public ResponseEntity<ApiResponse<String>> controlDeviceV1(String ip, String naturalId, Object data) {
        String url = buildUri(ip, API_V1, "control/" + naturalId);
        return executePut(url, data);
    }

    // --- Light Control  ---

    public ResponseEntity<ApiResponse<String>> controlLightPower(String ip, String naturalId, Object power) {
        return executePut(buildUrl(ip, "light", naturalId, "power"), power);
    }

    public ResponseEntity<ApiResponse<String>> controlLightLevel(String ip, String naturalId, Object level) {
        return executePut(buildUrl(ip, "light", naturalId, "level"), level);
    }

    // --- AC Control  ---
    public ResponseEntity<ApiResponse<String>> controlAcPower(String ip, String naturalId, Object power) {
        return executePut(buildUrl(ip, "ac", naturalId, "power"), power);
    }

    public ResponseEntity<ApiResponse<String>> controlAcTempUp(String ip, String naturalId, Object temp) {
        return executePut(buildUrl(ip, "ac", naturalId, "temp_up"), temp);
    }

    public ResponseEntity<ApiResponse<String>> controlAcTempDown(String ip, String naturalId, Object temp) {
        return executePut(buildUrl(ip, "ac", naturalId, "temp_down"), temp);
    }

    public ResponseEntity<ApiResponse<String>> controlAcMode(String ip, String naturalId, Object mode) {
        return executePut(buildUrl(ip, "ac", naturalId, "mode"), mode);
    }

    public ResponseEntity<ApiResponse<String>> controlAcFan(String ip, String naturalId, Object fan) {
        return executePut(buildUrl(ip, "ac", naturalId, "fan"), fan);
    }

    public ResponseEntity<ApiResponse<String>> controlAcSwing(String ip, String naturalId, Object swing) {
        return executePut(buildUrl(ip, "ac", naturalId, "swing"), swing);
    }

    // --- Fan Control  ---
    public ResponseEntity<ApiResponse<String>> controlFanPower(String ip, String naturalId, Object power) {
        return executePut(buildUrl(ip, "fan", naturalId, "power"), power);
    }

    public ResponseEntity<ApiResponse<String>> controlFanSpeed(String ip, String naturalId, Object speed) {
        return executePut(buildUrl(ip, "fan", naturalId, "speed"), speed);
    }

    public ResponseEntity<ApiResponse<String>> controlFanMode(String ip, String naturalId, Object mode) {
        return executePut(buildUrl(ip, "fan", naturalId, "mode"), mode);
    }

    public ResponseEntity<ApiResponse<String>> controlFanSwing(String ip, String naturalId, Object swing) {
        return executePut(buildUrl(ip, "fan", naturalId, "swing"), swing);
    }

    public ResponseEntity<ApiResponse<String>> controlFanLight(String ip, String naturalId, Object light) {
        return executePut(buildUrl(ip, "fan", naturalId, "light"), light);
    }

    // --- Private Helper Methods ---

    private String buildUrl(String ip, String deviceType, String naturalId, String action) {
        return buildUri(ip, API_V2, String.format("%s/%s/%s", deviceType, naturalId, action));
    }

    private ResponseEntity<ApiResponse<String>> executePut(String url, Object data) {
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(Map.of("data", data));
            return restTemplate.exchange(url, HttpMethod.PUT, requestEntity, new ParameterizedTypeReference<ApiResponse<String>>() {});
    }
}