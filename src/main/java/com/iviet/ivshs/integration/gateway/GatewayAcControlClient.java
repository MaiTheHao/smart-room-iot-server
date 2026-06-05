package com.iviet.ivshs.integration.gateway;

import com.iviet.ivshs.dto.control.AcRemoteRequestPayload;
import com.iviet.ivshs.dto.system.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

@Service
public class GatewayAcControlClient extends GatewayDeviceControlClient {

    public GatewayAcControlClient(@Qualifier("GatewayControlRestTemplate") RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<ApiResponse<String>> controlAcRemote(String ip, String naturalId,
            AcRemoteRequestPayload payload) {
        String url = buildUrl(ip, "ac", naturalId, "remote");
        HttpEntity<AcRemoteRequestPayload> requestEntity = new HttpEntity<>(payload);
        return restTemplate.exchange(url, HttpMethod.PUT, requestEntity,
                new ParameterizedTypeReference<ApiResponse<String>>() {
                });
    }

    public ResponseEntity<ApiResponse<String>> controlAcPower(String ip, String naturalId, Object power) {
        return executePut(buildUrl(ip, "ac", naturalId, "power"), power);
    }

    public ResponseEntity<ApiResponse<String>> controlAcTempUp(String ip, String naturalId, Object temp) {
        return executePut(buildUrl(ip, "ac", naturalId, "temp_up"), temp);
    }

    public ResponseEntity<ApiResponse<String>> controlAcTempDown(String ip, String naturalId, Object temp) {
        return executePut(buildUrl(ip, "ac", naturalId, "temp_down"), temp);
    }

    public ResponseEntity<ApiResponse<String>> controlAcMode(String ip, String naturalId, Object mode) {
        return executePut(buildUrl(ip, "ac", naturalId, "mode"), mode);
    }

    public ResponseEntity<ApiResponse<String>> controlAcFan(String ip, String naturalId, Object fan) {
        return executePut(buildUrl(ip, "ac", naturalId, "fan"), fan);
    }

    public ResponseEntity<ApiResponse<String>> controlAcSwing(String ip, String naturalId, Object swing) {
        return executePut(buildUrl(ip, "ac", naturalId, "swing"), swing);
    }
}
