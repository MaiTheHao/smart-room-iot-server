package com.iviet.ivshs.service.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iviet.ivshs.dto.HealthCheckResponseDto;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.util.HttpClientUtil;
import org.springframework.stereotype.Service;

/**
 * Client service for system-level gateway operations.
 */
@Service
public class GatewaySystemClient extends GatewayBaseClient {

    public HttpClientUtil.Response<SetupRequest> fetchSetup(String ip) {
        String url = build(ip, API_V1, "setup");
        return HttpClientUtil.get(url, new TypeReference<SetupRequest>() {});
    }

    public HttpClientUtil.Response<HealthCheckResponseDto> fetchHealthCheck(String ip) {
        String url = build(ip, API_V1, "health-check");
        return HttpClientUtil.get(url, new TypeReference<HealthCheckResponseDto>() {}) ;
    }
}
