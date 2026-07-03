package com.iviet.ivshs.integration.gateway.impl.raspi;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.control.DeviceControlPayload;
import org.springframework.beans.factory.annotation.Qualifier;

@Service
public class RaspiLightControlClient extends RaspiDeviceControlClient {

    public RaspiLightControlClient(@Qualifier("GatewayControlRestTemplate")
    RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<ApiResponse<String>> controlLightPower(String ip, String naturalId, DeviceControlPayload payload) {
        return executePut(buildUrl(ip, "light", naturalId, "power"), payload);
    }

    public ResponseEntity<ApiResponse<String>> controlLightLevel(String ip, String naturalId, DeviceControlPayload payload) {
        return executePut(buildUrl(ip, "light", naturalId, "level"), payload);
    }
}
