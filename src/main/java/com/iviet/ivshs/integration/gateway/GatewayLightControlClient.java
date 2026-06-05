package com.iviet.ivshs.integration.gateway;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.iviet.ivshs.dto.common.ApiResponse;
import org.springframework.beans.factory.annotation.Qualifier;

@Service
public class GatewayLightControlClient extends GatewayDeviceControlClient {

    public GatewayLightControlClient(@Qualifier("GatewayControlRestTemplate")
    RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<ApiResponse<String>> controlLightPower(String ip, String naturalId, Object power) {
        return executePut(buildUrl(ip, "light", naturalId, "power"), power);
    }

    public ResponseEntity<ApiResponse<String>> controlLightLevel(String ip, String naturalId, Object level) {
        return executePut(buildUrl(ip, "light", naturalId, "level"), level);
    }
}
