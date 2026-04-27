package com.iviet.ivshs.service.client.gateway;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.GatewayLoginResponse;
import com.iviet.ivshs.dto.LoginDto;
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
public class GatewayAuthClient extends GatewayBaseClient {

    private final RestTemplate restTemplate;

    public ResponseEntity<ApiResponse<GatewayLoginResponse>> login(String ip, @NonNull LoginDto loginDto) {
        String url = buildUri(ip, API_V1, "auth/login");
        HttpEntity<LoginDto> request = new HttpEntity<>(loginDto);
        return restTemplate.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<ApiResponse<GatewayLoginResponse>>() {});
    }
}