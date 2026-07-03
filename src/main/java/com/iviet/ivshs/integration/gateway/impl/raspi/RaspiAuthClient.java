package com.iviet.ivshs.integration.gateway.impl.raspi;

import com.iviet.ivshs.integration.gateway.base.BaseGatewayClient;
import com.iviet.ivshs.dto.auth.GatewayLoginResponse;
import com.iviet.ivshs.dto.auth.LoginDto;
import com.iviet.ivshs.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class RaspiAuthClient extends BaseGatewayClient {

    private final RestTemplate restTemplate;

    public ResponseEntity<ApiResponse<GatewayLoginResponse>> login(String ip, @NonNull
    LoginDto loginDto) {
        String url = buildUri(ip, API_V2, "auth/login");
        HttpEntity<LoginDto> request = new HttpEntity<>(loginDto);
        return restTemplate.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<ApiResponse<GatewayLoginResponse>>() {});
    }
}
