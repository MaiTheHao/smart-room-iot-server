package com.iviet.ivshs.service.client;

import com.iviet.ivshs.util.HttpClientUtil;
import org.springframework.stereotype.Service;

/**
 * Client service for maintenance operations on gateways.
 */
@Service
public class GatewayMaintenanceClient extends GatewayBaseClient {

    public HttpClientUtil.Response<String> resetAcEnergy(String ip, String naturalId) {
        String url = build(ip, API_V1, "air-conditions/" + naturalId + "/reset");
        return HttpClientUtil.post(url, null);
    }

    public HttpClientUtil.Response<String> resetFanEnergy(String ip, String naturalId) {
        String url = build(ip, API_V1, "fans/" + naturalId + "/reset");
        return HttpClientUtil.post(url, null);
    }

    public HttpClientUtil.Response<String> resetLightEnergy(String ip, String naturalId) {
        String url = build(ip, API_V1, "lights/" + naturalId + "/reset");
        return HttpClientUtil.post(url, null);
    }

    public HttpClientUtil.Response<String> resetRoomEnergy(String ip, String naturalId) {
        String url = build(ip, API_V1, "power-consumptions/" + naturalId + "/reset");
        return HttpClientUtil.post(url, null);
    }
}
