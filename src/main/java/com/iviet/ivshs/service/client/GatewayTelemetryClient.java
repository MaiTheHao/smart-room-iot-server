package com.iviet.ivshs.service.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.EnergyMetricDto;
import com.iviet.ivshs.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Client service for fetching telemetry data from gateways.
 */
@Slf4j
@Service
public class GatewayTelemetryClient extends GatewayBaseClient {

    public HttpClientUtil.Response<ApiResponse<EnergyMetricDto>> fetchLightEnergyMetric(String ip, String naturalId) {
        String url = build(ip, API_V1, "lights/" + naturalId + "/telemetry");
        return HttpClientUtil.get(url, new TypeReference<ApiResponse<EnergyMetricDto>>() {});
    }

    public HttpClientUtil.Response<ApiResponse<EnergyMetricDto>> fetchFanEnergyMetric(String ip, String naturalId) {
        String url = build(ip, API_V1, "fans/" + naturalId + "/telemetry");
        return HttpClientUtil.get(url, new TypeReference<ApiResponse<EnergyMetricDto>>() {});
    }

    public HttpClientUtil.Response<ApiResponse<EnergyMetricDto>> fetchAcEnergyMetric(String ip, String naturalId) {
        String url = build(ip, API_V1, "air-conditions/" + naturalId + "/telemetry");
        return HttpClientUtil.get(url, new TypeReference<ApiResponse<EnergyMetricDto>>() {});
    }

    public HttpClientUtil.Response<ApiResponse<EnergyMetricDto>> fetchRoomEnergyMetric(String ip, String naturalId) {
        String url = build(ip, API_V1, "power-consumptions/" + naturalId + "/telemetry");
        return HttpClientUtil.get(url, new TypeReference<ApiResponse<EnergyMetricDto>>() {});
    }
}
