package com.iviet.ivshs.service.client.gateway;

import com.iviet.ivshs.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

@Service
public class GatewayFanControlClient extends GatewayDeviceControlClient {

    public GatewayFanControlClient(@Qualifier("GatewayControlRestTemplate") RestTemplate restTemplate) {
        super(restTemplate);
    }

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
}
