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

    @Qualifier("GatewayApiClient")
    private final RestTemplate restTemplate;

    // --- V1 Control ---

    public ResponseEntity<ApiResponse<String>> controlDeviceV1(String ip, String naturalId, Object data) {
        String url = buildUri(ip, API_V1, "control/" + naturalId);
        return executePut(url, data);
    }

    // --- Light Control V2 ---

    public ResponseEntity<ApiResponse<String>> controlLightPowerV2(String ip, String naturalId, Object power) {
        return executePut(buildV2Url(ip, "light", naturalId, "power"), power);
    }

    public ResponseEntity<ApiResponse<String>> controlLightLevelV2(String ip, String naturalId, Object level) {
        return executePut(buildV2Url(ip, "light", naturalId, "level"), level);
    }

    // --- AC Control V2 ---
    public ResponseEntity<ApiResponse<String>> controlAcPowerV2(String ip, String naturalId, Object power) {
        return executePut(buildV2Url(ip, "ac", naturalId, "power"), power);
    }

    public ResponseEntity<ApiResponse<String>> controlAcTempUpV2(String ip, String naturalId, Object temp) {
        return executePut(buildV2Url(ip, "ac", naturalId, "temp_up"), temp);
    }

    public ResponseEntity<ApiResponse<String>> controlAcTempDownV2(String ip, String naturalId, Object temp) {
        return executePut(buildV2Url(ip, "ac", naturalId, "temp_down"), temp);
    }

    public ResponseEntity<ApiResponse<String>> controlAcModeV2(String ip, String naturalId, Object mode) {
        return executePut(buildV2Url(ip, "ac", naturalId, "mode"), mode);
    }

    public ResponseEntity<ApiResponse<String>> controlAcFanV2(String ip, String naturalId, Object fan) {
        return executePut(buildV2Url(ip, "ac", naturalId, "fan"), fan);
    }

    public ResponseEntity<ApiResponse<String>> controlAcSwingV2(String ip, String naturalId, Object swing) {
        return executePut(buildV2Url(ip, "ac", naturalId, "swing"), swing);
    }

    // --- Fan Control V2 ---
    public ResponseEntity<ApiResponse<String>> controlFanPowerV2(String ip, String naturalId, Object power) {
        return executePut(buildV2Url(ip, "fan", naturalId, "power"), power);
    }

    public ResponseEntity<ApiResponse<String>> controlFanSpeedV2(String ip, String naturalId, Object speed) {
        return executePut(buildV2Url(ip, "fan", naturalId, "speed"), speed);
    }

    public ResponseEntity<ApiResponse<String>> controlFanModeV2(String ip, String naturalId, Object mode) {
        return executePut(buildV2Url(ip, "fan", naturalId, "mode"), mode);
    }

    public ResponseEntity<ApiResponse<String>> controlFanSwingV2(String ip, String naturalId, Object swing) {
        return executePut(buildV2Url(ip, "fan", naturalId, "swing"), swing);
    }

    public ResponseEntity<ApiResponse<String>> controlFanLightV2(String ip, String naturalId, Object light) {
        return executePut(buildV2Url(ip, "fan", naturalId, "light"), light);
    }

    // --- Private Helper Methods ---

    private String buildV2Url(String ip, String deviceType, String naturalId, String action) {
        return buildUri(ip, API_V2, String.format("%s/%s/%s", deviceType, naturalId, action));
    }

    private ResponseEntity<ApiResponse<String>> executePut(String url, Object data) {
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(Map.of("data", data));
        return restTemplate.exchange(url, HttpMethod.PUT, requestEntity, new ParameterizedTypeReference<ApiResponse<String>>() {});
    }
}