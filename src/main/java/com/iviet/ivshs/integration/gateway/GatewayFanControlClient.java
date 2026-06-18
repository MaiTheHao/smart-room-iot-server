package com.iviet.ivshs.integration.gateway;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.control.DeviceControlPayload;
import org.springframework.beans.factory.annotation.Qualifier;

@Service
public class GatewayFanControlClient extends GatewayDeviceControlClient {

    public GatewayFanControlClient(@Qualifier("GatewayControlRestTemplate")
    RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<ApiResponse<String>> controlFanPower(String ip, String naturalId, DeviceControlPayload payload) {
        return executePut(buildUrl(ip, "fan", naturalId, "power"), payload);
    }

    public ResponseEntity<ApiResponse<String>> controlFanSpeed(String ip, String naturalId, DeviceControlPayload payload) {
        return executePut(buildUrl(ip, "fan", naturalId, "speed"), payload);
    }

    public ResponseEntity<ApiResponse<String>> controlFanMode(String ip, String naturalId, DeviceControlPayload payload) {
        return executePut(buildUrl(ip, "fan", naturalId, "mode"), payload);
    }

    public ResponseEntity<ApiResponse<String>> controlFanSwing(String ip, String naturalId, DeviceControlPayload payload) {
        return executePut(buildUrl(ip, "fan", naturalId, "swing"), payload);
    }
}
