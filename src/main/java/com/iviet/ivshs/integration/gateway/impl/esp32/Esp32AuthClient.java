package com.iviet.ivshs.integration.gateway.impl.esp32;

import com.iviet.ivshs.dto.auth.GatewayLoginResponse;
import com.iviet.ivshs.dto.auth.LoginDto;
import com.iviet.ivshs.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class Esp32AuthClient extends Esp32BaseClient {

    @Qualifier("GatewayApiClient")
    private final RestTemplate restTemplate;

    public ResponseEntity<ApiResponse<GatewayLoginResponse>> login(String ip, LoginDto loginDto) {
        String url = buildEsp32Uri(ip, "auth/login");
        HttpEntity<LoginDto> request = new HttpEntity<>(loginDto);
        return restTemplate.exchange(url, HttpMethod.POST, request,
            new ParameterizedTypeReference<ApiResponse<GatewayLoginResponse>>() {});
    }
}
