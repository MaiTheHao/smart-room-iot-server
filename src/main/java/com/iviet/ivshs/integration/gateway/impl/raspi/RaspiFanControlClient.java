package com.iviet.ivshs.integration.gateway.impl.raspi;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.DeviceControlPayload;
import com.iviet.ivshs.integration.gateway.GatewayCommand;
import org.springframework.beans.factory.annotation.Qualifier;

@Service
public class RaspiFanControlClient extends RaspiDeviceControlClient {

    public RaspiFanControlClient(@Qualifier("GatewayControlRestTemplate")
    RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<ApiResponse<String>> controlFan(String ip, GatewayCommand command) {
        String naturalId = command.naturalId();
        Object power = command.param("power");
        Object speed = command.param("speed");
        Object mode = command.param("mode");
        Object swing = command.param("swing");

        if (power != null) {
            return executePut(buildUrl(ip, "fan", naturalId, "power"), 
                DeviceControlPayload.of(command.specificType(), command.duration(), power));
        }
        if (speed != null) {
            return executePut(buildUrl(ip, "fan", naturalId, "speed"), 
                DeviceControlPayload.of(command.specificType(), command.duration(), speed));
        }
        if (mode != null) {
            return executePut(buildUrl(ip, "fan", naturalId, "mode"), 
                DeviceControlPayload.of(command.specificType(), command.duration(), mode));
        }
        if (swing != null) {
            return executePut(buildUrl(ip, "fan", naturalId, "swing"), 
                DeviceControlPayload.of(command.specificType(), command.duration(), swing));
        }
        return null;
    }
}
