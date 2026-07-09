package com.iviet.ivshs.integration.gateway.impl.raspi;

import com.iviet.ivshs.integration.gateway.base.BaseGatewayClient;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.integration.gateway.GatewayCommand;
import com.iviet.ivshs.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

@Service
@RequiredArgsConstructor
public class RaspiMaintenanceClient extends BaseGatewayClient {

    @Qualifier("GatewayTelemetryRestTemplate")
    private final RestTemplate restTemplate;

    public ResponseEntity<ApiResponse<String>> resetEnergy(String ip, GatewayCommand command) {
        String pathSegment = resolveGatewayPath(command);
        String naturalId = command.naturalId();
        String endpoint;
        if ("power-consumption".equals(pathSegment)) {
            endpoint = String.format("power-consumption/resetEnergy/%s", naturalId);
        } else {
            endpoint = String.format("%s/%s/resetEnergy", pathSegment, naturalId);
        }
        return executeReset(ip, endpoint);
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

    private ResponseEntity<ApiResponse<String>> executeReset(String ip, String endpoint) {
        String url = buildUri(ip, API_V2, endpoint);
        return restTemplate.exchange(url, HttpMethod.PUT, HttpEntity.EMPTY, new ParameterizedTypeReference<ApiResponse<String>>() {});
    }
}
