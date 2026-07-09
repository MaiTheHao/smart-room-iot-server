package com.iviet.ivshs.integration.gateway.impl.raspi;

import com.iviet.ivshs.integration.gateway.base.BaseGatewayClient;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.EnergyMetricDto;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import com.iviet.ivshs.integration.gateway.GatewayCommand;
import com.iviet.ivshs.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaspiTelemetryClient extends BaseGatewayClient {

    @Qualifier("GatewayTelemetryRestTemplate")
    private final RestTemplate restTemplate;

    public ResponseEntity<ApiResponse<EnergyMetricDto>> fetchEnergyMetric(String ip, GatewayCommand command) {
        String pathSegment = resolveGatewayPath(command);
        return executeGetTelemetry(ip, String.format("%s/%s/telemetry", pathSegment, command.naturalId()));
    }

    private String resolveGatewayPath(GatewayCommand command) {
        String hint = command.metaGatewayPath();
        if (hint != null) return hint;
        return switch (command.category()) {
            case LIGHT -> "light";
            case FAN -> "fan";
            case AIR_CONDITION -> "ac";
            case POWER_CONSUMPTION -> "power-consumption";
            default -> throw new BadRequestException(
                "No gateway path mapping for DeviceCategory: " + command.category());
        };
    }

    public ResponseEntity<TelemetryResponseDto> fetchGlobalTelemetry(String ip) {
        String url = buildUri(ip, API_V2, "telemetry");
        return restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<TelemetryResponseDto>() {});
    }

    private ResponseEntity<ApiResponse<EnergyMetricDto>> executeGetTelemetry(String ip, String endpoint) {
        String url = buildUri(ip, API_V2, endpoint);
        return restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<EnergyMetricDto>>() {});
    }
}
