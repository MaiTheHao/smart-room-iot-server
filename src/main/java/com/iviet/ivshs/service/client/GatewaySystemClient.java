package com.iviet.ivshs.service.client;

import com.iviet.ivshs.util.HttpClientUtil;
import org.springframework.stereotype.Service;

/**
 * Client service for system-level gateway operations.
 */
@Service
public class GatewaySystemClient extends GatewayBaseClient {

    public HttpClientUtil.Response<String> setup(String ip, Object setupData) {
        String url = build(ip, API_V1, "setup");
        return HttpClientUtil.post(url, setupData);
    }

    public HttpClientUtil.Response<String> healthCheck(String ip) {
        String url = build(ip, API_V1, "health-check");
        return HttpClientUtil.get(url);
    }
}
