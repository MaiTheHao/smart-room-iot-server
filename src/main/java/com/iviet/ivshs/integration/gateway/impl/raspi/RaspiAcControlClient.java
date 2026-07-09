package com.iviet.ivshs.integration.gateway.impl.raspi;

import com.iviet.ivshs.dto.AcRemoteRequestPayload;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.integration.gateway.GatewayCommand;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

@Service
public class RaspiAcControlClient extends RaspiDeviceControlClient {

    public RaspiAcControlClient(@Qualifier("GatewayControlRestTemplate")
    RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<ApiResponse<String>> controlAc(String ip, GatewayCommand command) {
        String naturalId = command.naturalId();
        AcRemoteRequestPayload acPayload = buildAcPayload(command);
        Object power = command.param("power");

        String action = (power != null) ? "power" : "remote";
        String url = buildUrl(ip, "ac", naturalId, action);
        HttpEntity<AcRemoteRequestPayload> requestEntity = new HttpEntity<>(acPayload);
        return restTemplate.exchange(url, HttpMethod.PUT, requestEntity, new ParameterizedTypeReference<ApiResponse<String>>() {});
    }

    private AcRemoteRequestPayload buildAcPayload(GatewayCommand cmd) {
        return AcRemoteRequestPayload.builder()
            .power(stringParam(cmd, "power"))
            .temperature(intParam(cmd, "temperature"))
            .mode(stringParam(cmd, "mode"))
            .speed(intParam(cmd, "speed"))
            .swing(stringParam(cmd, "swing"))
            .duration(cmd.duration())
            .specificType(cmd.specificType())
            .build();
    }

    private String stringParam(GatewayCommand cmd, String key) {
        Object val = cmd.param(key);
        return val != null ? val.toString() : null;
    }

    private Integer intParam(GatewayCommand cmd, String key) {
        Object val = cmd.param(key);
        return val instanceof Number n ? n.intValue() : null;
    }
}
