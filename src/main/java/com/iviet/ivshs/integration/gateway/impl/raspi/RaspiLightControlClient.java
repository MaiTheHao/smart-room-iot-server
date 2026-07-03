package com.iviet.ivshs.integration.gateway.impl.raspi;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.control.DeviceControlPayload;
import com.iviet.ivshs.integration.gateway.GatewayCommand;
import org.springframework.beans.factory.annotation.Qualifier;

@Service
public class RaspiLightControlClient extends RaspiDeviceControlClient {

    public RaspiLightControlClient(@Qualifier("GatewayControlRestTemplate")
    RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<ApiResponse<String>> controlLight(String ip, GatewayCommand command) {
        String naturalId = command.naturalId();
        Object power = command.param("power");
        Object level = command.param("level");

        if (power != null) {
            return executePut(buildUrl(ip, "light", naturalId, "power"), 
                DeviceControlPayload.of(command.specificType(), power));
        }
        if (level != null) {
            return executePut(buildUrl(ip, "light", naturalId, "level"), 
                DeviceControlPayload.of(command.specificType(), level));
        }
        return null;
    }
}
