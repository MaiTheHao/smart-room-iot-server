package com.iviet.ivshs.integration.gateway;

import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.control.AcRemoteRequestPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

@Service
public class GatewayAcControlClient extends GatewayDeviceControlClient {

    public GatewayAcControlClient(@Qualifier("GatewayControlRestTemplate")
    RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<ApiResponse<String>> controlAcRemote(String ip, String naturalId, AcRemoteRequestPayload payload) {
        String url = buildUrl(ip, "ac", naturalId, "remote");
        HttpEntity<AcRemoteRequestPayload> requestEntity = new HttpEntity<>(payload);
        return restTemplate.exchange(url, HttpMethod.PUT, requestEntity, new ParameterizedTypeReference<ApiResponse<String>>() {});
    }

    public ResponseEntity<ApiResponse<String>> controlAcPower(String ip, String naturalId, AcRemoteRequestPayload payload) {
        String url = buildUrl(ip, "ac", naturalId, "power");
        HttpEntity<AcRemoteRequestPayload> requestEntity = new HttpEntity<>(payload);
        return restTemplate.exchange(url, HttpMethod.PUT, requestEntity, new ParameterizedTypeReference<ApiResponse<String>>() {});
    }
}
