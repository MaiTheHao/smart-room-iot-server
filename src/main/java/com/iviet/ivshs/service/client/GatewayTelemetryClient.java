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

    public HttpClientUtil.Response<ApiResponse<EnergyMetricDto>> getLightEnergy(String ip, String naturalId) {
        String url = build(ip, API_V1, "lights/" + naturalId + "/telemetry");
        return fetchEnergyMetric(url);
    }

    public HttpClientUtil.Response<ApiResponse<EnergyMetricDto>> getFanEnergy(String ip, String naturalId) {
        String url = build(ip, API_V1, "fans/" + naturalId + "/telemetry");
        return fetchEnergyMetric(url);
    }

    public HttpClientUtil.Response<ApiResponse<EnergyMetricDto>> getAcEnergy(String ip, String naturalId) {
        String url = build(ip, API_V1, "air-conditions/" + naturalId + "/telemetry");
        return fetchEnergyMetric(url);
    }

    public HttpClientUtil.Response<ApiResponse<EnergyMetricDto>> getRoomEnergy(String ip, String naturalId) {
        String url = build(ip, API_V1, "power-consumptions/" + naturalId + "/telemetry");
        return fetchEnergyMetric(url);
    }

    private HttpClientUtil.Response<ApiResponse<EnergyMetricDto>> fetchEnergyMetric(String url) {
        try {
            var typeRef = new TypeReference<ApiResponse<EnergyMetricDto>>() {};
            var response = HttpClientUtil.get(url, typeRef);
            return response;
        } catch (Exception e) {
            log.error("Failed to fetch EnergyMetric from {}: {}", url, e.getMessage());
            return null;
        }
    }

}
