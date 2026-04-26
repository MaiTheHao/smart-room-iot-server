package com.iviet.ivshs.service.client;

import com.iviet.ivshs.util.HttpClientUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Client service for device control operations on gateways.
 */
@Service
public class GatewayControlClient extends GatewayBaseClient {

    public HttpClientUtil.Response<String> controlDeviceV1(String ip, String naturalId, Object data) {
        String url = build(ip, API_V1, "control/" + naturalId);
        return HttpClientUtil.put(url, Map.of("data", data));
    }

    // Light Control V2
    public HttpClientUtil.Response<String> controlLightPowerV2(String ip, String naturalId, Object power) {
        String url = build(ip, API_V2, "light/" + naturalId + "/power");
        return HttpClientUtil.put(url, Map.of("data", power));
    }

    public HttpClientUtil.Response<String> controlLightLevelV2(String ip, String naturalId, Object level) {
        String url = build(ip, API_V2, "light/" + naturalId + "/level");
        return HttpClientUtil.put(url, Map.of("data", level));
    }

    // AC Control V2
    public HttpClientUtil.Response<String> controlAcPowerV2(String ip, String naturalId, Object power) {
        String url = build(ip, API_V2, "ac/" + naturalId + "/power");
        return HttpClientUtil.put(url, Map.of("data", power));
    }

    public HttpClientUtil.Response<String> controlAcTempUpV2(String ip, String naturalId, Object temp) {
        String url = build(ip, API_V2, "ac/" + naturalId + "/temp_up");
        return HttpClientUtil.put(url, Map.of("data", temp));
    }

    public HttpClientUtil.Response<String> controlAcTempDownV2(String ip, String naturalId, Object temp) {
        String url = build(ip, API_V2, "ac/" + naturalId + "/temp_down");
        return HttpClientUtil.put(url, Map.of("data", temp));
    }

    public HttpClientUtil.Response<String> controlAcModeV2(String ip, String naturalId, Object mode) {
        String url = build(ip, API_V2, "ac/" + naturalId + "/mode");
        return HttpClientUtil.put(url, Map.of("data", mode));
    }

    public HttpClientUtil.Response<String> controlAcFanV2(String ip, String naturalId, Object fan) {
        String url = build(ip, API_V2, "ac/" + naturalId + "/fan");
        return HttpClientUtil.put(url, Map.of("data", fan));
    }

    public HttpClientUtil.Response<String> controlAcSwingV2(String ip, String naturalId, Object swing) {
        String url = build(ip, API_V2, "ac/" + naturalId + "/swing");
        return HttpClientUtil.put(url, Map.of("data", swing));
    }
}
