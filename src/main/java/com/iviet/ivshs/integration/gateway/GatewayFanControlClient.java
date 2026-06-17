package com.iviet.ivshs.integration.gateway;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.iviet.ivshs.dto.common.ApiResponse;
import org.springframework.beans.factory.annotation.Qualifier;

@Service
public class GatewayFanControlClient extends GatewayDeviceControlClient {

    public GatewayFanControlClient(@Qualifier("GatewayControlRestTemplate")
    RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<ApiResponse<String>> controlFanPower(String ip, String naturalId, Object power, String specificType, Integer duration) {
        return executePut(buildUrl(ip, "fan", naturalId, "power"), power, specificType, duration);
    }

    public ResponseEntity<ApiResponse<String>> controlFanSpeed(String ip, String naturalId, Object speed, String specificType, Integer duration) {
        return executePut(buildUrl(ip, "fan", naturalId, "speed"), speed, specificType, duration);
    }

    public ResponseEntity<ApiResponse<String>> controlFanMode(String ip, String naturalId, Object mode, String specificType, Integer duration) {
        return executePut(buildUrl(ip, "fan", naturalId, "mode"), mode, specificType, duration);
    }

    public ResponseEntity<ApiResponse<String>> controlFanSwing(String ip, String naturalId, Object swing, String specificType, Integer duration) {
        return executePut(buildUrl(ip, "fan", naturalId, "swing"), swing, specificType, duration);
    }
}
